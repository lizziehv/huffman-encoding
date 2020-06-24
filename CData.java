/**
 * @author Lizzie Hernandez Videa
 * A class that holds data for nodes in Huffman code tree
 */

public class CData {
    private char character;
    private int frequency;

    /**
     * @param frequency keeps track of frequency of children
     *                  doesn't take a character parameter
     */
    public CData(int frequency){
        this.frequency = frequency;
    }

    /**
     * Constructor for type of data to be contained inside each node of binary tree
     * @param character value of char of node
     * @param frequency number of times character appears in text
     */
    public CData(char character, int frequency){
        this.character = character;
        this.frequency = frequency;
    }

    // setters
    public void setCharacter(char character){this.character = character;}
    public void setFrequency(int frequency){this.frequency = frequency;}

    // getters
    public char getCharacter(){return character;}
    public int getFrequency(){return frequency;}

    // Override default toString()
    public String toString(){
        return character + ":" + frequency;
    }
}
