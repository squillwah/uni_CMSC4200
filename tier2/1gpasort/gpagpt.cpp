#include <iostream>
#include <cstdlib>
#include <ctime>
#include <string>

using namespace std;

//#define LISTSIZE 100000000
#define LISTSIZE 10000
// Make sure to unlimit your stack: ulimit -s unlimited

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

    student output[LISTSIZE];
    int count[401] = {0};

    for (int i = 0; i < LISTSIZE; i++) {
        int key = (int)(students[i].gpa * 100 + 0.5);
        count[key]++;
    }

    for (int i = 1; i < 401; i++) {
        count[i] += count[i - 1];
    }

    for (int i = LISTSIZE - 1; i >= 0; i--) {
        int key = (int)(students[i].gpa * 100 + 0.5);
        output[count[key] - 1] = students[i];
        count[key]--;
    }

    for (int i = 0; i < LISTSIZE; i++) {
        students[i] = output[i];
    }

    std::cout << "(";
    for (int i = 0; i < LISTSIZE; i++) std::cout << students[i].gpa << " ";
    std::cout << ")" << std::endl;

    return 0;
}