#include <iostream>
#include <cstdlib>
#include <ctime>
#include <string>

using namespace std;

//#define LISTSIZE 100000000
#define LISTSIZE 10000
// Make sure to unlimit your stack: ulimit -s unlimited

//  global
struct student {
    string name;
    int age;
    int id;
    float gpa;
    string major;
};

student genStudent() {
    string names[6] = {"Bob", "Tim", "Alice", "Hannah", "Alex", "Jack"};
    string majors[6] = {"CMSC", "MATH", "PSYC", "BIOL", "PHYS", "CHEM"};

    student s;
    s.name = names[rand() % 6];
    s.age = rand() % 101;
    //s.id = rand();
    s.gpa = (rand() % 401) / 100.0;
    s.major = majors[rand() % 6];

    return s;
}

int main(void) {
    srand(time(0));
    int target = 10000; //  set to max for testing, worst case

    //  removed for timing reason
    /*
    cout << "Target ID: ";
    cin >> target;
    */
    student students[LISTSIZE];
    for (int i = 0; i < LISTSIZE; i++) {
        students[i] = genStudent();
        students[i].id = i;
    }

    for (int i = 0; i < LISTSIZE; i++) {
        if(students[i].id == target) {
            cout << students[i].name << endl;
        }
    }
    

    return 0;
}