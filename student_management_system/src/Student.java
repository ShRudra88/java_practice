public class Student {
     private int id;
    private String name;
    private String department;

    public Student(int id, String name, String department)
    {
        this.id = id;
        this.name = name;
        this.department = department;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getDepartment()
    {
        return department;
    }

    public void display()
    {
        System.out.println("Id : " +id);
        System.out.println("Name : " +name);
        System.out.println("Departement "+department);

    }

}