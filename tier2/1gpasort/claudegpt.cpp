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

    student tmp;
    int lo = 0, hi = LISTSIZE - 1;

    while (lo < hi) {
        int lastSwap = lo;

        // Left-to-right pass: bubbles the largest unsorted element to the right
        for (int i = lo; i < hi; i++) {
            if (students[i].gpa > students[i+1].gpa) {
                tmp = students[i+1];
                students[i+1] = students[i];
                students[i] = tmp;
                lastSwap = i;
            }
        }
        hi = lastSwap; // everything past lastSwap is in final position

        // Right-to-left pass: bubbles the smallest unsorted element to the left
        for (int i = hi; i > lo; i--) {
            if (students[i-1].gpa > students[i].gpa) {
                tmp = students[i-1];
                students[i-1] = students[i];
                students[i] = tmp;
                lastSwap = i;
            }
        }
        lo = lastSwap; // everything before lastSwap is in final position
    }

    std::cout << "(";
    for (int i = 0; i < LISTSIZE; i++) std::cout << students[i].gpa << " ";
    std::cout << ")" << std::endl;
    return 0;
}