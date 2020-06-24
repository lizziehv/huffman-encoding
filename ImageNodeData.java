public class ImageNodeData {
    private int color;
    private int frequency;

    /**
     * @param frequency keeps track of frequency of children
     *                  doesn't take a color parameter
     */
    public ImageNodeData(int frequency){
        this.frequency = frequency;
    }

    /**
     * Constructor for type of data to be contained inside each node of binary tree
     * @param color value of color at node
     * @param frequency number of times color appears in image
     */
    public ImageNodeData(int color, int frequency){
        this.color = color;
        this.frequency = frequency;
    }

    // setters
    public void setColor(int color){this.color = color;}
    public void setFrequency(int frequency){this.frequency = frequency;}

    // getters
    public int getColor(){return color;}
    public int getFrequency(){return frequency;}

    // Override default toString()
    public String toString(){
        return color + ":" + frequency;
    }
}
