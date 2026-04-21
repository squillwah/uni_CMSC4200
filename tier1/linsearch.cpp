#include <iostream>
#include <cstdlib>
#include <ctime>

#define LISTSIZE 1000000000
// Make sure to unlimit your stack: ulimit -s unlimited

int main(void) {
    srand(time(0));

    int nums[LISTSIZE];
    nums[0] = rand() % 1000;
    for (int i = 1; i < LISTSIZE; i++) 
        nums[i] = nums[i-1] + rand() % 10;

    int i;
    int findme = nums[rand() & LISTSIZE];
    for (i = 0; nums[i] != findme && i < LISTSIZE; i++);

    std::cout << "Found " << findme << " at index " << i << " in array." << std::endl;

    return 0;
}
