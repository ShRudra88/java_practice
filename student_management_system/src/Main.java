import java.util.Scanner;

public class Main { public static void main(String[] args) {

    Scanner sc = new Scanner(System.in);
    StudentService service = new StudentService();

    while (true) {
        System.out.println("\n===== Student Management System =====");
        System.out.println("1. Add Student");
        System.out.println("2. Show All Students");
        System.out.println("3. Search Student");
        System.out.println("4. Delete Student");
        System.out.println("5. Exit");
        System.out.print("Enter your choice: ");

        int choice = sc.nextInt();
        sc.nextLine(); 

        switch (choice) {

            case 1:
                System.out.print("Enter Student ID: ");
                int id = sc.nextInt();
                sc.nextLine();

                System.out.print("Enter Student Name: ");
                String name = sc.nextLine();

                System.out.print("Enter Department: ");
                String department = sc.nextLine();

                Student student = new Student(id, name, department);
                service.addStudent(student);
                break;

            case 2:
                System.out.println("\n===== Student List =====");
                service.showStudents();
                break;

            case 3:
                System.out.print("Enter Student ID to search: ");
                int searchId = sc.nextInt();

                service.searchStudent(searchId);
                break;

            case 4:
                System.out.print("Enter Student ID to delete: ");
                int deleteId = sc.nextInt();

                service.deleteStudent(deleteId);
                break;

            case 5:
                System.out.println("Thank you for using Student Management System!");
                sc.close();
                return;

            default:
                System.out.println("Invalid choice! Please try again.");
        }
    }
}


}