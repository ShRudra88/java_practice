public class SalaryCalculator {

public double calculateHRA(double basicSalary) {
    return basicSalary * 0.20;
}

public double calculateMedicalAllowance(double basicSalary) {
    return basicSalary * 0.10;
}

public double calculateTransportAllowance(double basicSalary) {
    return basicSalary * 0.05;
}

public double calculateTax(double basicSalary) {
    return basicSalary * 0.05;
}

public double calculateNetSalary(double basicSalary) {

    double hra = calculateHRA(basicSalary);
    double medical = calculateMedicalAllowance(basicSalary);
    double transport = calculateTransportAllowance(basicSalary);
    double tax = calculateTax(basicSalary);

    return basicSalary + hra + medical + transport - tax;
}

public void displaySalaryDetails(Employee employee) {

    double basic = employee.getBasicSalary();

    double hra = calculateHRA(basic);
    double medical = calculateMedicalAllowance(basic);
    double transport = calculateTransportAllowance(basic);
    double tax = calculateTax(basic);
    double netSalary = calculateNetSalary(basic);

    System.out.println("\n========== Salary Details ==========");
    employee.display();

    System.out.printf("HRA: %.2f%n", hra);
    System.out.printf("Medical Allowance: %.2f%n", medical);
    System.out.printf("Transport Allowance: %.2f%n", transport);
    System.out.printf("Tax Deduction: %.2f%n", tax);

    System.out.println("------------------------------------");
    System.out.printf("Net Salary: %.2f%n", netSalary);
    System.out.println("====================================");
}


}