import java.util.ArrayList;
public class StudentService {
    private ArrayList<Student> students = new ArrayList<>();

    public void addStudent(Student student)
    {
        students.add(student);
        System.out.println("Student added successfully");

    }

    public void showStudents(){
        if(students.isEmpty()){
            System.out.println("No students found");
            return;
        }

        for(Student student : students){
            student.display();
            System.out.println("--------------");
        }
    }

    public void searchStudent(int id)
        {
            for(Student student: students){
                if(student.getId() == id){
                    student.display();
                    return;
                }
            }
            System.out.println("Student not found");
        }

    public void deleteStudent(int id){
            boolean removed = students.removeIf(
                student -> student.getId() == id
            );

            if(removed)System.out.println("Student deleted");
            else System.out.println("Student not found");
        }
    

}