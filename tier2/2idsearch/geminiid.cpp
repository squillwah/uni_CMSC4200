#include <iostream>
#include <cstdlib>
#include <ctime>
#include <string>

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
    s.gpa = (rand() % 401) / 100.0;
    s.major = majors[rand() % 6];

    return s;
}

int main(void) {
    srand(time(0));
    int target;

    cout << "Target ID: ";
    cin >> target;

    student students[LISTSIZE];
    for (int i = 0; i < LISTSIZE; i++) {
        students[i] = genStudent();
        students[i].id = i; // Array is implicitly sorted by ID
    }

    // --- OPTIMIZED SEARCH START ---
    int low = 0;
    int high = LISTSIZE - 1;
    bool found = false;

    while (low <= high) {
        int mid = low + (high - low) / 2; // Prevents potential overflow

        if (students[mid].id == target) {
            cout << students[mid].name << endl;
            found = true;
            break; 
        } else if (students[mid].id < target) {
            low = mid + 1;
        } else {
            high = mid - 1;
        }
    }
    
    if (!found) {
        cout << "Student not found." << endl;
    }
    // --- OPTIMIZED SEARCH END ---

    return 0;
}