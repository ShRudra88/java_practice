
public class Designation {

    private int designationId;
    private String designationName;

    // Constructor
    public Designation(int designationId, String designationName) {
        this.designationId = designationId;
        this.designationName = designationName;
    }

    // Display Designation Information
    public void displayDesignation() {
        System.out.println("Designation ID: " + designationId);
        System.out.println("Designation: " + designationName);
    }
}
