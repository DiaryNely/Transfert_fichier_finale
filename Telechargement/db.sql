CREATE SEQUENCE seq_editionsal_id
  START WITH 1
  INCREMENT BY 1
  NOCYCLE;

CREATE TABLE EDITIONSAL (
  ID NUMBER ,
  EMPNO NUMBER,
  MOIS NUMBER,
  ANNEE NUMBER,
  MONTANT NUMBER,
  RUBRIQUE VARCHAR2(50),
  CONSTRAINT pk_editionsal PRIMARY KEY (ID),
  CONSTRAINT fk_editionsal_emp FOREIGN KEY (EMPNO) REFERENCES EMP(EMPNO)
);

CREATE TABLE EMP_HISTORY (
  HISTORYDATE DATE,
  SAL NUMBER,
  STATUT NUMBER,
  EMPNO NUMBER,
  CONSTRAINT fk_emp_history_emp FOREIGN KEY (EMPNO) REFERENCES EMP(EMPNO)
);

-- Suppression de toutes les valeurs de la table
BEGIN
    EXECUTE IMMEDIATE 'DELETE FROM EDITIONSAL';
    COMMIT;  -- Valider la suppression
END;
/

-- Réinitialisation de la séquence
BEGIN
    EXECUTE IMMEDIATE 'ALTER SEQUENCE SEQ_EDITIONSAL_ID RESTART WITH 1';
    COMMIT;  -- Valider la réinitialisation
END;
/



CREATE TABLE LOANS (
    EMPNO NUMBER NOT NULL,                   -- Employee number (foreign key referencing EMP table)
    MONTANT NUMBER NOT NULL,         -- Amount of the loan
    POURCENTAGE NUMBER NOT NULL,             -- Interest percentage on the loan
    MOIS0 NUMBER NOT NULL CHECK (MOIS0 BETWEEN 1 AND 12), -- Starting month of the loan (1-12)
    ANNEE0 NUMBER NOT NULL,                  -- Starting year of the loan
    ACTIVE CHAR(1) DEFAULT 'Y',              -- Indicates if the loan is currently active ('Y' for Yes, 'N' for No)
    FOREIGN KEY (EMPNO) REFERENCES EMP(EMPNO) -- Foreign key constraint to EMP table
);



INSERT INTO EMP (EMPNO, ENAME, JOB, MGR, HIREDATE, SAL, COMM, DEPTNO) 
VALUES (4646, 'ALAIN', 'MIASA', 7654, TO_DATE('2024-01-01', 'YYYY-MM-DD'), 1200, 1, 10);