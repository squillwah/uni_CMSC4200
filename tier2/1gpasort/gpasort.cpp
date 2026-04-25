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
    s.id = rand();
    s.gpa = (rand() % 401) / 100.0;
    s.major = majors[rand() % 6];

    return s;
}

int main(void) {
    srand(time(0));

    student students[LISTSIZE];
    for (int i = 0; i < LISTSIZE; i++) 
        students[i] = genStudent();

    /*
    std::cout << "(";
    for (int i = 0; i < LISTSIZE - 1; i++) std::cout << students[i].gpa << " ";
    std::cout << ")" << std::endl;
    */

    student tmp;
    int sorted = 0;
    while (!sorted) {
        sorted = 1;
        for (int i = 0; i < LISTSIZE - 1; i++) {
            if (students[i].gpa > students[i+1].gpa) {
                sorted = 0;
                tmp = students[i+1];
                students[i+1] = students[i];
                students[i] = tmp;
            }
        }
    }

    std::cout << "(";
    for (int i = 0; i < LISTSIZE; i++) std::cout << students[i].gpa << " ";
    std::cout << ")" << std::endl;

    return 0;
}