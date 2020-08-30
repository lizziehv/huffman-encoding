import javax.imageio.ImageIO;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.StringTokenizer;
import java.awt.image.*;


public class ImageCompression extends DrawingGUI{


    private static boolean debugFlag = false;         // used to check if the code is working
                                                      // 't' for test cases, 'w' for war and peace, 'u' for us constitution
    // delimiters used for tree string (helpful for parsing a Newick tree)
    private static String delimiter1 = String.valueOf((char)(29));
    private static String delimiter2 = String.valueOf((char)(30));
    private static String delimiter3 = String.valueOf((char)(31));
    // delimiter to determine when tree string ends and compressed file begins
    private static char endOfTreeIndicator = (char)(28);
    // used in Newick parsing to take care of boundary case
    private static Boolean hasRightChild;


    /**
     * @param img name of image
     * @return a map with all colors in an image(keys as an int) and their corresponding frequency(value)
     */
    public static Map<Integer, Integer> frequencyTable(BufferedImage img){
        // create a map to be returned with all characters and their frequency
        Map<Integer, Integer> colorFrequencies = new HashMap<>();

        // Open the image file

        // Read the image colors pixel by pixel
        for(int j = 0; j< img.getHeight(); j++){
            for(int i = 0; i< img.getWidth(); i++){
                int c = img.getRGB(i, j);
                // if it's already in the map add 1 to the frequency
                if(colorFrequencies.containsKey(c)){
                    colorFrequencies.put(c, colorFrequencies.get(c)+1);
                }
                // else create a new key for the character with value 1
                else{
                    colorFrequencies.put(c, 1);
                }
            }
        }

        // used for debugging purposes
        // print the frequency Map
        if (debugFlag) {
            System.out.println(colorFrequencies);}

        return colorFrequencies;
    }

    /**
     * @param img name of image
     * @return a queue with binary trees for every color in the image
     */
    public static PriorityQueue<BinaryTree<ImageNodeData>> colorFrequencyQueue(BufferedImage img){
        // use function that returns a map with all characters and their frequencies
        Map<Integer, Integer> frequencyTable = frequencyTable(img);
        // create a queue to be returned and pass it a compare function as a parameter
        PriorityQueue<BinaryTree<ImageNodeData>> queue = new PriorityQueue<>(new ColorComparator());

        // make sure there are characters in the file
        if(!frequencyTable.isEmpty()){
            for(Object c: frequencyTable.keySet()){
                // create a new node with character and frequency given by the map
                ImageNodeData d = new ImageNodeData((int)c, frequencyTable.get(c));
                BinaryTree<ImageNodeData> b = new BinaryTree<>(d);
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
     * @param img image name
     * @return Huffman code tree, binary tree which ranks higher priority colors
     */
    public static BinaryTree<ImageNodeData> colorTree(BufferedImage img){
        // create a new tree to be returned
        BinaryTree<ImageNodeData> colorTree;
        // construct queue with a node for every character
        PriorityQueue<BinaryTree<ImageNodeData>> queue = colorFrequencyQueue(img);

        // make sure there are elements inside the queue
        if(queue.size() == 0){ return null;}
        // keep a case for a text with a single character
        else if (queue.size() == 1){
            // get the single character
            BinaryTree<ImageNodeData> t1 = queue.remove();
            // create a tree with a node containing the character's frequency and the character key as a child
            ImageNodeData newData = new ImageNodeData(t1.getData().getFrequency());
            BinaryTree<ImageNodeData> t = new BinaryTree<>(newData, t1, null);

            // add it to the queue to be returned
            queue.add(t);
        }
        else {
            // for case when queue size > 1
            while (queue.size() > 1) {
                // extract the two with least frequency
                BinaryTree<ImageNodeData> t1 = queue.remove();
                BinaryTree<ImageNodeData> t2 = queue.remove();

                // "join" the nodes and keep track of their priority sum with a node
                ImageNodeData newData = new ImageNodeData(t1.getData().getFrequency() + t2.getData().getFrequency());
                BinaryTree<ImageNodeData> t = new BinaryTree<>(newData, t1, t2);

                // add new "big" binary tree to queue
                queue.add(t);
            }
        }

        // last binary tree left is tree to be returned
        colorTree = queue.remove();

        // used for debugging purposes
        // print the code tree
        if (debugFlag) {
            System.out.println(colorTree);}

        return colorTree;
    }

    /**
     * @param tree to retrieve codes from
     * @return a map with all characters as key and path of how to get there in a Huffman tree as value
     */
    public static Map<Integer, String> codeRetrieval(BinaryTree<ImageNodeData> tree){
        // instantiate map to be returned
        Map<Integer, String> pathsEncoding = new HashMap<>();
        // call function that creates a binary tree base on character priority
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
    public static void codeRetrievalHelper(Map<Integer,String> pathsEncoding, BinaryTree<ImageNodeData> tree, String pathSoFar){
        // if the file is empty
        if (tree.size() == 0) { return; }
        // if it is a character node, add it to the map
        if(tree.isLeaf()){
            pathsEncoding.put(tree.getData().getColor(), pathSoFar);
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
     * @param tree tree to parse into a string
     * @return sring ((c:#, c:#):#,(c:#, c:#):#):#
     */

    public static String treeToString(BinaryTree<ImageNodeData> tree){
        String treeString = "";
        if(tree == null){
            return delimiter1+delimiter2+delimiter3+endOfTreeIndicator;
        }
        if(!tree.isLeaf()){
            treeString = delimiter1;
            if(tree.hasLeft()) {
                treeString += treeToString(tree.getLeft());
            }

            treeString += delimiter2;

            if(tree.hasRight()){

                treeString += treeToString(tree.getRight());
            }
            treeString += delimiter3 + ":"+ tree.getData().getFrequency();

            return treeString;
        }
        else{
            return tree.getData().toString();
        }
    }


    /**
     * Writes a sequence of 0's and 1's to a compressed file
     * @param imagePathName the path name for the original image we wish to compress
     */
    public static void compress(String imagePathName){
        // create map with all characters and string with path
        BufferedImage img = loadImage(imagePathName);
        BinaryTree<ImageNodeData> tree = colorTree(img);
        Map<Integer, String> codeMap = codeRetrieval(tree);
        BufferedBitWriterEC output;
        // direction for compressed file
        String compressedPathName = imagePathName.substring(0, imagePathName.length()-4) + "_compressedEC.txt";


        // Open the output file, if possible
        try {
            output = new BufferedBitWriterEC(compressedPathName);
        }
        catch (IOException e) {
            System.err.println("Cannot open file.\n" + e.getMessage());
            return;
        }

        // go over input file and write over output file
        try {
            // write dimensions of the image
            output.writeString(img.getWidth()+"x"+img.getHeight()+ delimiter1);
            // write tree used to compress, to be retrieved when decompressed
            output.writeString(treeToString(tree) + endOfTreeIndicator);


            // Read the image colors pixel by pixel
            for (int j = 0; j < img.getHeight(); j++) {
                for (int i = 0; i < img.getWidth(); i++) {
                    //Retrieve its code from the map
                    String colorCode = codeMap.get(img.getRGB(i, j));

                    for (int k = 0; k < colorCode.length(); k++) {
                        // if it's to the right in the binary tree
                        if (colorCode.charAt(k) == '1') {
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
    }

    /**
     * @param s tree string to parse into a string
     * @return tree built from string
     */
    public static BinaryTree<ImageNodeData> parseNewick(String s) {
        // boundary case: empty file
        if(s.equals(delimiter1+delimiter2+delimiter3)){
            return new BinaryTree<>(null);
        }

        BinaryTree<ImageNodeData> t = parseNewick(new StringTokenizer(s, delimiter1+delimiter2+delimiter3, true));
        // Get rid of the semicolon
        return t;
    }

    /**
     * Does the real work of parsing, now given a tokenizer for the string
     */
    public static BinaryTree<ImageNodeData> parseNewick(StringTokenizer st) {
        String token = st.nextToken();

        if (token.equals(delimiter1)) {
            // Inner node
            BinaryTree<ImageNodeData> left = parseNewick(st);
            String comma = st.nextToken();
            hasRightChild = true;
            BinaryTree<ImageNodeData> right = parseNewick(st);
            if(hasRightChild) {
                String close = st.nextToken();
            }
            String label = st.nextToken();

            String[] pieces = label.split(":");
            int frequency;

            if(hasRightChild) {
                frequency = left.getData().getFrequency() + right.getData().getFrequency();
            } else{
                frequency = left.getData().getFrequency();
            }
            return new BinaryTree<ImageNodeData>(new ImageNodeData(frequency), left, right);
        }

        // Leaf
        else {

            if (token.equals(delimiter3)){
                hasRightChild = false;
                return new BinaryTree<>(null);
            }

            else{
                String[] pieces = token.split(":");
                return new BinaryTree<ImageNodeData>(new ImageNodeData(Integer.parseInt(pieces[0]), Integer.parseInt(pieces[1])), null, null);
            }
        }
    }


    /**
     * Method used to decompress a file, writes decompressed bits to output file
     * @param fileName file to be decompressed
     */
    public static void decompress(String fileName) {
        // retrieve binary tree with characters as leaves
        BinaryTree<ImageNodeData> huffmanTree;

        BufferedImage result;
        BufferedBitReaderEC input;

        // where to send decompressed file
        String decompressedPathName = fileName.substring(0, fileName.length() - 17) + "_decompressedEC.png";

        // Open the input file, if possible, else stop process
        try {
            input = new BufferedBitReaderEC(fileName);
        } catch (IOException e) {
            System.err.println("IO error while reading.\n" + e.getMessage());
            return;
        }

        // go over input file and write over blank image
        try {
            // get the dimensions of the image, written in the first line of the compressed file
            String imageDimensions = "";
            char c = input.readCharacter();

            while (!String.valueOf(c).equals(delimiter1)) {
                imageDimensions += c;
                c = input.readCharacter();
            }
            String[] dimensions = imageDimensions.split("x");
            // create blank result with same dimensions
            result = new BufferedImage(Integer.parseInt(dimensions[0]), Integer.parseInt(dimensions[1]), BufferedImage.TYPE_INT_ARGB);

            System.out.println("___________");

            // retrieve tree used to compare
            String treeString = "";
            char current = input.readCharacter();
            while (current != endOfTreeIndicator) {

                treeString += current;
                current = input.readCharacter();
            }
            huffmanTree = parseNewick(treeString);

            // start at head of binary tree
            BinaryTree<ImageNodeData> t = huffmanTree;
            int x = 0;          // pixel x coordinate
            int y = 0;          // pixel y coordinate
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
                        result.setRGB(x, y, t.getData().getColor());
                        t = huffmanTree;

                        x += 1;

                        // used for debugging
                        if (debugFlag) {
                            System.out.println("found");
                        }
                    }
                    // end of image row: restart x value, move in y
                    if (x == Integer.parseInt(dimensions[0])) {
                        x = 0;
                        y += 1;
                    }

                }

            }
        } catch (IOException e) {
            System.err.println("IO error while reading.\n" + e.getMessage());
            return;
        }

        // Close the input file, if possible
        try {
            input.close();
        } catch (IOException e) {
            System.err.println("Cannot close file.\n" + e.getMessage());
        }

        try {
            ImageIO.write(result, "png", new File(decompressedPathName));
            System.out.println("Saved decompress image in " + decompressedPathName);
        } catch (Exception e) {
            System.err.println("Couldn't save snapshot in `" + decompressedPathName + "' -- make sure the folder exists");
        }


    }

}
