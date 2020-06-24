import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.io.*;


public class Huffman {

    private static boolean debugFlag = false;         // used to check if the code is working
    private static char file = 'u';                   // determines which files to compress/decompress
                                                      // 't' for test cases, 'w' for war and peace, 'u' for us constitution

    /**
     * @param fileName file to read characters from
     * @return a map with all characters in the text(keys) and their corresponding frequency(value)
     */

    public static Map<Character, Integer> frequencyTable(String fileName){
        // create a map to be returned with all characters and their frequency
        Map<Character, Integer> characterFrequencies = new HashMap<>();
        BufferedReader input;

        // Open the file, if possible
        try {
            input = new BufferedReader(new FileReader(fileName));
        }
        catch (FileNotFoundException e) {
            System.err.println("Cannot open file.\n" + e.getMessage());
            return characterFrequencies;
        }

        // Read the file
        try {
            // Character by character
            int c;
            while ((c = input.read()) != -1) {
                // if it's already in the map add 1 to the frequency
                if (characterFrequencies.containsKey((char)c)) {
                    characterFrequencies.put((char)(c), characterFrequencies.get((char)c)+1);
                }
                // else create a new key for the character with value 1
                else {
                    characterFrequencies.put((char)c, 1);
                }
            }
        }
        catch (IOException e) {
            System.err.println("IO error while reading.\n" + e.getMessage());
        }

        // Close the file, if possible
        try {
            input.close();
        }
        catch (IOException e) {
            System.err.println("Cannot close file.\n" + e.getMessage());
        }

        // used for debugging purposes
        // print the frequency Map
        if (debugFlag) {
            System.out.println(characterFrequencies);}

        return characterFrequencies;
    }

    /**
     * @param fileName file to be read
     * @return a queue with binary trees for every character
     */
    public static PriorityQueue<BinaryTree<CData>> characterFrequency(String fileName){
        // use function that returns a map with all characters and their frequencies
        Map frequencyTable = frequencyTable(fileName);
        // create a queue to be returned and pass it a compare function as a parameter
        PriorityQueue<BinaryTree<CData>> queue = new PriorityQueue<>(new TreeComparator());

        // make sure there are characters in the file
        if(!frequencyTable.isEmpty()){
            for(Object c: frequencyTable.keySet()){
                // create a new node with character and frequency given by the map
                CData d = new CData((char) c, (int)frequencyTable.get(c));
                BinaryTree<CData> b = new BinaryTree<>(d);
                // add tree (node) to the queue
                queue.add(b);
            }
        }

        // used for debugging purposes
        // print the priority queue
        if (debugFlag) {
            System.out.println(queue);}

        return queue;
    }

    /**
     * @param fileName file to be read
     * @return Huffman code tree, binary tree which ranks higher priority characters
     */
    public static BinaryTree<CData> tree(String fileName){
        // create a new tree to be returned
        BinaryTree<CData> characterTree;
        // construct queue with a node for every character
        PriorityQueue<BinaryTree<CData>> queue = characterFrequency(fileName);

        // make sure there are elements inside the queue
        if(queue.size() == 0){ return null;}
        // keep a case for a text with a single character
        else if (queue.size() == 1){
            // get the single character
            BinaryTree<CData> t1 = queue.remove();
            // create a tree with a node containing the character's frequency and the character key as a child
            CData newData = new CData(t1.getData().getFrequency());
            BinaryTree<CData> t = new BinaryTree<>(newData, t1, null);

            // add it to the queue to be returned
            queue.add(t);
        }
        else {
            // for case when queue size > 1
            while (queue.size() > 1) {
                // extract the two with least frequency
                BinaryTree<CData> t1 = queue.remove();
                BinaryTree<CData> t2 = queue.remove();

                // "join" the nodes and keep track of their priority sum with a node
                CData newData = new CData(t1.getData().getFrequency() + t2.getData().getFrequency());
                BinaryTree<CData> t = new BinaryTree<>(newData, t1, t2);

                // add new "big" binary tree to queue
                queue.add(t);
            }
        }

        // last binary tree left is tree to be returned
        characterTree = queue.remove();

        // used for debugging purposes
        // print the code tree
        if (debugFlag) {
            System.out.println(characterTree);}

        return characterTree;
    }

    /**
     * @param fileName pass it the name of a file to be read
     * @return a map with all characters as key and path of how to get there in a Huffman tree as value
     */
    public static Map<Character, String> codeRetrieval(String fileName){
        // instantiate map to be returned
        Map<Character, String> pathsEncoding = new HashMap<>();
        // call function that creates a binary tree base on character priority
        BinaryTree<CData> tree = tree(fileName);
        if (tree != null){
            // if tree has elements, add to the map all keys and values by calling helper function
            codeRetrievalHelper(pathsEncoding, tree, "");
        }

        // used for debugging purposes
        // print the code map
        if (debugFlag) {
            System.out.println(pathsEncoding);}

        return pathsEncoding;
    }

    /**
     * Helper method to create map with all characters and path to be followed in binary tree
     * @param pathsEncoding map that keeps all keys(characters) and values(paths)
     * @param tree previously created binary tree base on character priority
     * @param pathSoFar keeps track of a string of 0s and 1s to determine path that should be taken to get to the
     *                 character in the binary tree
     */
    public static void codeRetrievalHelper(Map<Character,String> pathsEncoding, BinaryTree<CData> tree, String pathSoFar){
        // if the file is empty
        if (tree.size() == 0) { return; }
        // if it is a character node, add it to the map
        if(tree.isLeaf()){
            pathsEncoding.put(tree.getData().getCharacter(), pathSoFar);
        }
        else {
            // recursive calls of function on right and left children
            if (tree.hasLeft()) {
                // remember to add to the string(0 is left and 1 is right)
                codeRetrievalHelper(pathsEncoding, tree.getLeft(), pathSoFar + '0');
            }
            if (tree.hasRight()) {
                codeRetrievalHelper(pathsEncoding, tree.getRight(), pathSoFar + '1');
            }
        }
    }

    /**
     * Writes a sequence of 0's and 1's to a compressed file
     * @param fileName the path name for the original document we wish to compress
     */
    public static void compress(String fileName){
        // create map with all characters and string with path
        Map<Character, String> codeMap = codeRetrieval(fileName);
        BufferedBitWriter output;
        BufferedReader input;
        // direction for compressed file
        String compressedPathName = fileName.substring(0, fileName.length()-4) + "_compressed.txt";

        // Open the input file, if possible, else stop process
        try {
            input = new BufferedReader(new FileReader(fileName));
        }
        catch (FileNotFoundException e) {
            System.err.println("Cannot open file.\n" + e.getMessage());
            return;
        }

        // Open the output file, if possible
        try {
            output = new BufferedBitWriter(compressedPathName);
        }
        catch (IOException e) {
            System.err.println("Cannot open file.\n" + e.getMessage());
            return;
        }

        // go over input file and write over output file
        try{
            // Character by character
            int c;
            // as long as there are characters to read
            while ((c = input.read()) != -1) {
                // find in map
                String characterCode = codeMap.get((char)c);
                // used for debugging purposes,
                // to see if compression works
                if (debugFlag) {
                    System.out.println(characterCode);}
                // loop over code to convert to bit
                for(int i = 0; i< characterCode.length(); i++){
                    // if it's to the right in the binary tree
                    if(characterCode.charAt(i) == '1'){
                        output.writeBit(true);

                        // used for debugging purposes,
                        // to see if compression works
                        if (debugFlag) {
                            System.out.println("true");

                        }
                    }
                    // if to the left
                    else {
                        output.writeBit(false);

                        // used for debugging purposes,
                        // to see if compression works
                        if (debugFlag) {
                            System.out.println("false");
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            System.err.println("IO error while reading.\n" + e.getMessage());
        }

        // Close the output file, if possible
        try{
            output.close();
        }
        catch (IOException e) {
                System.err.println("Cannot close file.\n" + e.getMessage());
        }

        // Close the input file, if possible
        try {
            input.close();
        }
        catch (IOException e) {
            System.err.println("Cannot close file.\n" + e.getMessage());
        }

    }

    /**
     * Method used to decompress a file, writes decompressed bits to output file
     * @param fileName file to be decompressed
     */


    public static void decompress(String fileName) {
        // retrieve binary tree with characters as leaves
        BinaryTree<CData> huffmanTree = tree(fileName);

        BufferedWriter output;
        BufferedBitReader input;

        // where to get compressed file from
        String inputPathName = fileName.substring(0, fileName.length() - 4) + "_compressed.txt";
        // where to send decompressed file
        String decompressedPathName = fileName.substring(0, fileName.length() - 4) + "_decompressed.txt";

        // Open the input file, if possible, else stop process
        try {
            input = new BufferedBitReader(inputPathName);
        } catch (IOException e) {
            System.err.println("IO error while reading.\n" + e.getMessage());
            return;
        }

        // Open the output file, if possible
        try {
            output = new BufferedWriter(new FileWriter(decompressedPathName));
        } catch (IOException e) {
            System.err.println("Cannot open file.\n" + e.getMessage());
            return;
        }

        // go over input file and write over output file
        try {
            // start at head of binary tree
            BinaryTree<CData> t = huffmanTree;

            while (input.hasNext()) {
                boolean bit = input.readBit();                     // read bit
                // have not yet gotten to the end of the character code
                if (!t.isLeaf()) {
                    // if bit is a 1, move right on the tree
                    if (bit) {
                        t = t.getRight();

                        // used for debugging purposes,
                        // to see if decompression works
                        if (debugFlag) {
                            System.out.println("right");
                        }
                    }
                    // if bit is a 0, move left on the tree
                    else {
                        t = t.getLeft();

                        // used for debugging purposes,
                        // to see if decompression works
                        if (debugFlag) {
                            System.out.println("left");
                        }
                    }

                    // check to see if we have found the character, now that we moved
                    if (t.isLeaf()) {
                        // write to decompressed file and restart current tree node for next search
                        output.write(t.getData().getCharacter());
                        t = huffmanTree;

                        // used for debugging
                        if (debugFlag) {
                            System.out.println("found");
                        }
                    }
                }

            }

        } catch (IOException e) {
            System.err.println("IO error while reading.\n" + e.getMessage());
        }

        // Close the output file, if possible
        try {
            output.close();
        } catch (IOException e) {
            System.err.println("Cannot close file.\n" + e.getMessage());
        }

        // Close the input file, if possible
        try {
            input.close();
        } catch (IOException e) {
            System.err.println("Cannot close file.\n" + e.getMessage());
        }
    }
}
