## 인덱스 구조 및 탐색

### 인덱스 튜닝

- 인덱스 스캔 과정에서 발생하는 비효율을 줄이는 것.    
- 테이블 액세스 횟수를 줄이는 것. 

### 인덱스 구조

- 인덱스 : 대용량 테이블에서 필요한 데이터만 빠르게 효율적으로 액세스하기 위해 사용하는 오브젝트.
- ROWID는 데이터 블록 주소(DBA, Data Block Address)와 로우 번호로 구성되므로 이 값을 알면 테이블 레코드를 찾아갈 수 있음.
- 인덱스 탐색 과정
    - 수직적 탐색 : 인덱스 스캔 시작지점을 찾는 과정
        - 정렬된 인덱스 레코드 중 조건에 만족하는 첫 번째 레코드를 찾는 과정. → 인덱스 스캔 시작지점을 찾는 과정.
    - 수평적 탐색 : 데이터를 찾는 과정
        - 수직적 탐색을 통해 스캔 시작점을 찾았다면, 찾고자 하는 데이터가 더 안 나타날 때까지 인덱스 리프 블록을 수평적으로 탐색함.
- **인덱스 컬럼을 가공하면 인덱스를 정상적으로 사용(Range Scan) 할 수 없음.**
- **분명한 스캔 시작지점과 종료지점이 있으면, 수직적 탐색에 해당.**

### 인덱스를 이용한 소트 연산 생략

- 인덱스를 Range Scan 할 수 있는 이유는 데이터가 정렬되어 있음.
  ex) PK가 장비번호 + 변경일자 + 변경순번 순으로 구성된 상태변경이력 테이블이 있음.

    ```sql
    SELECT * FROM 상태변경이력 WHERE 장비번호 = 'C' AND 변경일자 = '20180316'
    
    Execution Plan
    --------------------------------------------
    0     SELECT STATEMENT Optimizer=ALL_ROWS (Cost=85 Card=81 Bytes=5K)
    1  0    TABLE ACCESS (BY INDEX ROWID) OF ' 상태변경이력' (TABLE) (Cost=85)
    2  1      INDEX (RANGE SCAN) OF '상태변경이력_PK' (INDEX (UNIQUE)) (Cost=3)
    ```

    - 위의 쿼리와 같이 장비번호/변경일자 모두 ‘=‘’ 조건으로 검색할 때 PK 인덱스를 사용하면 결과집합은 변경순번 순으로 출력됨.

<br>    

## ORDER BY 절에서 컬럼 가공

- 인덱스 컬럼은 대개 조건절에 사용한 컬럼을 의미하는데, 인덱스 컬럼을 가공하면 인덱스를 정상적으로 사용할 수 없음.
- PK 인덱스를 장비번호 + 변경일자 + 변경순번으로 구성했다면, 아래 SQL은 정렬 연산을 생략할 수 없음.
    
    ```sql
    SELECT * FROM 상태변경이력 WHERE 장비번호 = 'C' ORDER BY 변경일자 || 변경순번
    ```

### SELECT-LIST에서 컬럼 가공

- 인덱스를 장비번호 + 변경일자 + 변경순번 순으로 구성하면, 변경순번 최소/최대 값을 구할 때도 옵티마이저는 정렬 연산을 따로 수행하지 않음.

    ```sql
    SELECT MIN(변경순번) FROM 상태변경이력 WHERE 장비번호 = 'C' AND 변경일자 = '20180306'
    SELECT MAX(변경순번) FROM 상태변경이력 WHERE 장비번호 = 'C' AND 변경일자 = '20180306'
    ```

- 인덱스에는 문자열 기준으로 정렬돼 있는데, 이를 숫자값으로 바꾼 값 기준으로 요구한 경우 정렬 연산을 생략할 수 없음.

### 자동 형변환

- 각 조건절에서 양쪽 값의 데이터 타입이 서로 다르면 값을 비교할 수 없으며, 그럴 떄 타입 체크를 엄격히 함으로써 컴파일 시점에 에러를 내는 DBMS가 있는가 하면, 자동으로 형변환 처리해주는 DBMS도 있음. → 오라클은 자동으로 형변환함.
- 오라클에서 숫자형과 문자형이 만나면 숫자형이 이김. → 숫자형 컬럼 기준으로 문자형 컬럼을 변환함.
- 날짜형과 문자형이 만나면 날짜형이 이김.
    - LIKE 자체가 문자열 비교 연산자이므로 이때는 문자형 기준으로 숫자형 컬럼이 변환됨.
        
        ```sql
        SELECT * FROM 고객 WHERE 고객번호 LIKE '9410%'
        
        Excution Plan
        ------------------------------------------------------------
        0     SELECT STATEMENT Optimizer=ALL_ROWS (Cost=3 Card=1 Bytes=38)
        1  0    TABLE ACCESS (FULL) OF '고객' (TABLE) (Cost=3 Card=1 Bytes=38)
        ------------------------------------------------------------
        
        Predicate information (identified by opreation id) :
        ------------------------------------------------------------
        1 - filter(TO_CHAR("고객번호") LIKE '9418%')
        ```
<br>        

## 인덱스 확장기능 사용법

### Index Range Scan

- B-Tree 인덱스의 가장 일반적이고 정상적인 형태의 액세스 방식.
- 인덱스 루트에서 리프 블록까지 수직적으로 탐색한 후 필요한 범위만 스캔함.
    
    ```sql
    set autotrace traceonly exp
    
    select * from emp where deptno = 20;
    
    Excution Plan
    ----------------------------------------------
    0      SELECT STATEMENT Optimizer=ALL_ROWS
    1  0     TABLE ACCESS (BY INDEX ROWID) OF 'EMP' (TABLE)
    2  1       INDEX (RANGE SCAN) OF 'EMP_DEPTNO_IDX' (INDEX)
    ```
    

### Index Full Scan

- 수직적 탐색 없이 인덱스 리프 블록을 처음부터 끝까지 수평적으로 탐색하는 방식.
    
    ```sql
    create index emp_ename_sal_idx on emp(ename, sal);
    
    set autotrace traceonly exp
    
    select * from emp where sal > 2000 order by ename;
    
    Excution Plan
    ----------------------------------------------
    0      SELECT STATEMENT Optimizer=ALL_ROWS
    1  0     TABLE ACCESS (BY INDEX ROWID) OF 'EMP' (TABLE)
    2  1       INDEX (FULL SCAN) OF 'EMP_ENAME_SAL_IDX' (INDEX)
    ```
    
- Index Full Scan은 대게 데이터 검색을 위한 최적의 인덱스가 없을 때 차선으로 선택됨.

### Index Unique Scan

- 수직적 탐색만으로 데이터를 찾는 스캔 방식으로서, Unique 인덱스를 '=' 조건으로 탐색하는 경우에 작동함.
    
    ```sql
    create unique index pk_emp on emp(empno);
    
    alter table emp add constraint pk_emp primary key(empno) using index pk_emp;
    
    set autotrace traceonly explain
    
    select empno, ename from emp where empno = 7788;
    
    Excution Plan
    ----------------------------------------------
    0      SELECT STATEMENT Optimizer=ALL_ROWS
    1  0     TABLE ACCESS (BY INDEX ROWID) OF 'EMP'
    2  1       INDEX (UNIQUE SCAN) OF 'PK_EMP' (UNIQUE)
    ```

### Index Skip Scan

- Index Skip Scan은 루트 또는 브랜치 블록에서 읽은 컬럼 값 정보를 이용해 조건절에 부합하는 레코드를 포함할 '가능성이 있는' 리프 블록만 골라서 액세스하는 스캔 방식.

### Index Fast Full Scan

- Index Fast Full Scan은 Index Full Scan보다 빠름.
    - 인덱스 세그먼트 전체를 Multiblock I/O 방식으로 스캔하기 때문.
- Index Fast Full Scan은 Multiblock I/O 방식을 사용하므로 디스크로부터 대량의 인덱스 블록을 읽어야 할 때 효과적.
- 속도는 빠르지만 결과집합이 인덱스 키 순서대로 정렬되지 않음.

### Index Range Scan Decending

- Index Range Scan과 기본적으로 동일한 스캔 방식.
- 인덱스를 뒤에서부터 앞으로 스캔하기 때문에 내림차순으로 정렬된 결과집합을 얻는다는 점이 차이점.
