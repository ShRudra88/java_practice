-- Run as the application schema owner while the application is stopped.
-- Existing employee and department rows are preserved; the current EMPLOYEES
-- DEPARTMENT_ID foreign key already references DEPARTMENTS.
DECLARE
    v_start NUMBER;
    v_exists NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_exists
    FROM user_sequences
    WHERE sequence_name = 'NEXORA_EMPLOYEES_SEQ';

    IF v_exists = 0 THEN
        SELECT NVL(MAX(employee_id), 0) + 1 INTO v_start FROM employees;
        EXECUTE IMMEDIATE
            'CREATE SEQUENCE NEXORA_EMPLOYEES_SEQ START WITH '
            || TO_CHAR(v_start, 'FM99999999999999999999999999999999999999')
            || ' INCREMENT BY 1 NOCYCLE';
    END IF;

    SELECT COUNT(*) INTO v_exists
    FROM user_sequences
    WHERE sequence_name = 'NEXORA_DEPARTMENTS_SEQ';

    IF v_exists = 0 THEN
        SELECT NVL(MAX(department_id), 0) + 1 INTO v_start FROM departments;
        EXECUTE IMMEDIATE
            'CREATE SEQUENCE NEXORA_DEPARTMENTS_SEQ START WITH '
            || TO_CHAR(v_start, 'FM99999999999999999999999999999999999999')
            || ' INCREMENT BY 1 NOCYCLE';
    END IF;
END;
/
