package de.enough.polish.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.enough.polish.io.Externalizable;

/**
 * A trie is a data structure for efficient matching of words in a search text.
 * <dl>
 * <dt>Responsibilities:
 * <dd>
 * <dt>Life Cycle:
 * <dd>The object is created with new.
 * <dt>Statefulness:
 * <dd>The object is stateful. Once created, it contains the added words until its end.
 * <dt>Parents and Children:
 * <dd>The object is not managed by another object. It does not manage other objects.
 * <dt>Volatility:
 * <dd>The object is in-memory only.
 * <dt>Synchronization:
 * <dd>The methods of the object are not thread-safe.
 * <dt>Configuration:
 * <dd>The words to match must be provided, either in the constructor or with {@link #addWord(String)}.
 * <dt>Dependencies:
 * <dd>For this object to be useful, another object should implement {@link #Trie.TrieSearchConsumer} to receive matched words.
 * </dl>
 * 
 * @author rickyn
 */
public class Trie implements Externalizable {

	/**
	 * A callback object which is called after a word in a search text was found.
	 * 
	 * @author rickyn
	 */
	public interface TrieSearchConsumer {

		/**
		 * @param searchText The search text in which the matched word was found.
		 * @param matchedWord The word which was found.
		 * @param matchedWordIndex The index in the search text at which the matched word was found.
		 */
		public void onWordFound(String searchText, String matchedWord, int matchedWordIndex);
	}

	/**
	 * A node object is the building block of the trie. The have a child-sibling organization to prevent dynamic management of children.
	 * 
	 * @author rickyn
	 */
	static class Node {
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.character;
			result = prime * result + ((this.firstChild == null) ? 0 : this.firstChild.hashCode());
			result = prime * result + ((this.nextSibling == null) ? 0 : this.nextSibling.hashCode());
			result = prime * result + ((this.word == null) ? 0 : this.word.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Node other = (Node) obj;
			if (this.character != other.character)
				return false;
			if (this.firstChild == null) {
				if (other.firstChild != null)
					return false;
			} else if (!this.firstChild.equals(other.firstChild))
				return false;
			if (this.nextSibling == null) {
				if (other.nextSibling != null)
					return false;
			} else if (!this.nextSibling.equals(other.nextSibling))
				return false;
			if (this.word == null) {
				if (other.word != null)
					return false;
			} else if (!this.word.equals(other.word))
				return false;
			return true;
		}

		public char character;
		public Node nextSibling;
		public Node firstChild;
		public String word;

		public Node(char character) {
			this.character = character;
		}

		public String toString() {
			return "Node = {character:'" + this.character + "',word:'" + this.word + "'}";
		}

		public void print(int indent, StringBuffer buffer) {
			for (int i = 0; i < indent; i++) {
				buffer.append(" ");
			}
			buffer.append(toString());
			buffer.append("\n");
			if (this.firstChild != null) {
				for (int i = 0; i < indent; i++) {
					buffer.append(" ");
				}
				this.firstChild.print(indent + 2, buffer);
			}
			if (this.nextSibling != null) {
				for (int i = 0; i < indent; i++) {
					buffer.append(" ");
				}
				this.nextSibling.print(indent, buffer);
			}
		}

		// TODO: Implement equals and hashcode. This will help with validating the serialization mechanism.
	}

	private static final short LATEST_FORMAT_VERSION = 1;
	private static final short FORMAT_INDICATOR_CHILD = 1;
	private static final short FORMAT_INDICATOR_SIBLING = 2;
	private static final short FORMAT_INDICATOR_LEAF = 3;

	private Node root = new Node((char) 0);
	private boolean longestMatchOption = true;

	/**
	 * Construct a Trie object and add the given initial words.
	 * 
	 * @param words The Array parameter must not be null and must not contain null elements or empty strings.
	 * @see #addWord(String)
	 * @throws IllegalArgumentException Thrown if the parameter is null.
	 */
	public Trie(String[] words) {
		if (words == null) {
			throw new IllegalArgumentException("The parameter 'words' must not be null.");
		}
		for (int i = 0; i < words.length; i++) {
			addWord(words[i]);
		}
	}

	/**
	 * Construct a Trie object without any words registered.
	 * 
	 * @see #addWord(String)
	 */
	public Trie() {
		//
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		this.root.print(0, buffer);
		return buffer.toString();
	}

	/**
	 * Register a word in this Trie to be found when a search text is searched with {@link #search(String, TrieSearchConsumer)}.
	 * 
	 * @param word The parameter must not be null or empty.
	 * @throws IllegalArgumentException Thrown if the parameter is invalid.
	 */
	public void addWord(String word) {
		if (word == null) {
			throw new IllegalArgumentException("The parameter 'word' must not be null.");
		}
		if (word.length() == 0) {
			throw new IllegalArgumentException("The String parameter 'word' must not be empty.");
		}
		int wordLength = word.length();
		char currentCharacter;
		Node currentNode = this.root;
		Node previousNode = currentNode;
		for (int i = 0; i < wordLength; i++) {
			currentCharacter = word.charAt(i);
			if (currentNode == null) {
				currentNode = new Node(currentCharacter);
				previousNode.firstChild = currentNode;
			}
			while (currentNode != null) {
				if (currentCharacter == currentNode.character) {
					break;
				} else {
					previousNode = currentNode;
					currentNode = currentNode.nextSibling;
				}
			}
			if (currentNode == null) {
				currentNode = new Node(currentCharacter);
				previousNode.nextSibling = currentNode;
			}
			if (i == wordLength - 1) {
				// The last character.
				currentNode.word = word;
			}
			previousNode = currentNode;
			currentNode = currentNode.firstChild;
		}
	}

	/**
	 * Search the given text to match words registered in this Trie object and notify the consumer about the findings by calling the given
	 * callback object.
	 * 
	 * @param text The parameter must not be null though the string may be empty.
	 * @param trieSearchConsumer If the parameter is null, no search is performed.
	 * @throws IllegalArgumentException Thrown if the parameter is invalid.
	 */
	public void search(String text, TrieSearchConsumer trieSearchConsumer) {
		if (text == null) {
			throw new IllegalArgumentException("The parameter 'text' must not be null.");
		}
		if (trieSearchConsumer == null) {
			return;
		}
		int textLength = text.length();
		int searchIndex;
		char currentCharacter;
		for (int textIndex = 0; textIndex < textLength; textIndex++) {
			Node currentNode = this.root;
			currentCharacter = text.charAt(textIndex);
			searchIndex = textIndex;
			String lastFoundWord = null;
			do {
				if (currentCharacter == currentNode.character) {
					if (currentNode.word != null) {
						if (this.longestMatchOption) {
							// Just remember the word. We report it later.
							lastFoundWord = currentNode.word;
						} else {
							trieSearchConsumer.onWordFound(text, currentNode.word, textIndex);
						}
					}
					searchIndex += 1;
					if (searchIndex >= textLength) {
						break;
					}
					currentCharacter = text.charAt(searchIndex);
					currentNode = currentNode.firstChild;
				} else {
					currentNode = currentNode.nextSibling;
				}
			} while (currentNode != null);
			if (this.longestMatchOption) {
				if (lastFoundWord != null) {
					trieSearchConsumer.onWordFound(text, lastFoundWord, textIndex);
					lastFoundWord = null;
				}
			}
		}
	}

	/**
	 * Matching a word in a text can use a shortest or longest match mechanism.
	 * 
	 * @param longestMatchOption Value true if the longest matching word should be found in the text. Value false if every word should be
	 * matched even if it is a prefix to another word like :) to :)).
	 */
	public void setLongestMatchOption(boolean longestMatchOption) {
		this.longestMatchOption = longestMatchOption;
	}

	public void write(DataOutputStream out) throws IOException {
		out.writeShort(LATEST_FORMAT_VERSION);
		Node stackElement = new Node((char) 0);
		stackElement.firstChild = this.root;
		Node topElement = stackElement;
		out.writeShort(FORMAT_INDICATOR_SIBLING);
		while (topElement != null) {
			// Write yourself in the stream
			out.write(topElement.character);
			// Handle child of Trie node
			if (topElement.firstChild.firstChild != null) {
				stackElement = new Node((char) 0);
				stackElement.nextSibling = topElement;
				stackElement.firstChild = topElement.firstChild.firstChild;
				topElement = stackElement;
				out.writeShort(FORMAT_INDICATOR_CHILD);
				continue;
			}
			// Handle sibling
			if (topElement.firstChild.nextSibling != null) {
				topElement.firstChild = topElement.firstChild.nextSibling;
				out.writeShort(FORMAT_INDICATOR_SIBLING);
				continue;
			}
			out.writeShort(FORMAT_INDICATOR_LEAF);
			topElement = topElement.nextSibling;
			// Is the stack empty, then we are done.
			if (topElement != null) {
				topElement.firstChild = null;
			}
		}

	}

	/**
	 * Initializes an empty Trie object with a serialized version.
	 * 
	 * @throws IllegalStateException Thrown if the trie object has already words added.
	 * @throws IOException Thrown if the format version of the serialized trie is not supported.
	 * @see de.enough.polish.io.Externalizable#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream in) throws IOException {
		if (!(this.root.firstChild == null && this.root.nextSibling == null)) {
			throw new IllegalStateException("This object is already initialized with words. Only an empty Trie object can be read into. Create an empty Trie object with 'new Trie()'");
		}
		short version = in.readShort();
		if (version != LATEST_FORMAT_VERSION) {
			throw new IOException("The format version of the input is '" + version + "'. Only version '" + LATEST_FORMAT_VERSION + "' is supported. Convert the input or implement the unsupported version.");
		}
		char currentTrieCharacter;
		Node currentTrieNode;
		Node topElement = new Node((char) 0);
		// The empty root node of the trie.
		topElement.firstChild = new Node((char) 0);
		short command;
		while (topElement != null) {
			command = in.readShort();
			if (command == FORMAT_INDICATOR_LEAF) {
				// TODO: Poppen und child ausketten.
				continue;
			}

			currentTrieCharacter = in.readChar();
			currentTrieNode = new Node(currentTrieCharacter);
			
			Node stackElement = new Node((char) 0);
			stackElement.firstChild = currentTrieNode;
			stackElement.nextSibling = topElement;

			switch (command) {
			case FORMAT_INDICATOR_CHILD:
				topElement.firstChild = currentTrieNode;
				break;
			case FORMAT_INDICATOR_SIBLING:
				topElement.nextSibling = currentTrieNode;
				break;
			default:
				throw new IOException("Encountered command '" + command + "'. This is not a valid command. Make sure to remove this command from the input.");
			}

			topElement = stackElement;
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.longestMatchOption ? 1231 : 1237);
		result = prime * result + ((this.root == null) ? 0 : this.root.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Trie other = (Trie) obj;
		if (this.longestMatchOption != other.longestMatchOption)
			return false;
		if (this.root == null) {
			if (other.root != null)
				return false;
		} else if (!this.root.equals(other.root))
			return false;
		return true;
	}
	
	

}
