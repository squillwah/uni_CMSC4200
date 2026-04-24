#include <iostream>
#include <cstdlib>
#include <ctime>

//#define LISTSIZE 100000000
#define LISTSIZE 100000
// Make sure to unlimit your stack: ulimit -s unlimited

int main(void) {
    srand(time(0));

    int nums[LISTSIZE];
    for (int i = 0; i < LISTSIZE; i++) 
        nums[i] = rand() % LISTSIZE*10;
    
    std::cout << "Starting..." << std::endl;

    int i;
    int tmp;
    int sorted = 0;
    while (!sorted) {
        sorted = 1;
        for (i = 0; i < LISTSIZE-1; i++) {
            if (nums[i] > nums[i+1]) {
                sorted = 0;
                tmp = nums[i+1];
                nums[i+1] = nums[i];
                nums[i] = tmp;
            }
        }
    }
    
    std::cout << "Done!" << std::endl;

    return 0;
}
