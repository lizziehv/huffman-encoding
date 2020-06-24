/**
 * @author Lizzie Hernandez
 * Function that determines how two binary tree nodes should be compared for Huffman code
 */

import java.util.Comparator;

public class TreeComparator implements Comparator<BinaryTree<CData>>{

    /**
     * @param b1 first node to compare frequency
     * @param b2 node to be compared with b1
     * @return an int (0 if the frequencies are the same, -1 if b1 has a lower frequency, 1 if b2 has a lower frequency)
     */
    public int compare(BinaryTree<CData> b1, BinaryTree<CData> b2){
        // get the difference between both frequencies
        int difference = b1.getData().getFrequency() - b2.getData().getFrequency();

        if(difference < 0){return -1;}      // if frequency of b1 < frequency of b2
        else if( difference > 0){return 1;} // if frequency of b2 < frequency of b1
        else{ return 0;}                    // if their frequencies are equal
    }
}
