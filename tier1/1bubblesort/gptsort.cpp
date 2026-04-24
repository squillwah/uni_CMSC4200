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

    int* counts = (int*)calloc(LISTSIZE, sizeof(int));

    for (int i = 0; i < LISTSIZE; i++) {
        counts[nums[i] / 10]++;
    }

    int pos = 0;
    for (int value = 0; value < LISTSIZE; value++) {
        while (counts[value] > 0) {
            nums[pos++] = value * 10;
            counts[value]--;
        }
    }

    free(counts);

    std::cout << "Done!" << std::endl;

    return 0;
}