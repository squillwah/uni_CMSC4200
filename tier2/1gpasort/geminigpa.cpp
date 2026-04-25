#include <iostream>
#include <cstdlib>
#include <ctime>
#include <string>
#include <algorithm> // Essential for std::sort

using namespace std;

#define LISTSIZE 10000

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

// Comparator function for sorting
bool compareGPA(const student& a, const student& b) {
    return a.gpa < b.gpa;
}

int main(void) {
    srand(time(0));

    // Using static allocation as per your original code
    // Note: for LISTSIZE 100,000,000, you MUST use 'static' or heap allocation
    // to avoid a Stack Overflow, even with ulimit changes.
    static student students[LISTSIZE]; 
    
    for (int i = 0; i < LISTSIZE; i++) 
        students[i] = genStudent();

    // OPTIMIZATION: std::sort uses Introsort (O(n log n))
    // It is significantly faster than the manual Bubble Sort.
    std::sort(students, students + LISTSIZE, compareGPA);

    std::cout << "(";
    for (int i = 0; i < LISTSIZE; i++) std::cout << students[i].gpa << " ";
    std::cout << ")" << std::endl;

    return 0;
}