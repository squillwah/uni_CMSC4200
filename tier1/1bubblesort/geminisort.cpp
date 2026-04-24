#include <iostream>
#include <cstdlib>
#include <ctime>
#include <algorithm> // Note: If you cannot add this, see the QuickSort version below

#define LISTSIZE 100000

int main(void) {
    srand(time(0));

    // Array creation and population (Do not change)
    int nums[LISTSIZE];
    for (int i = 0; i < LISTSIZE; i++) 
        nums[i] = rand() % LISTSIZE*10;
    
    std::cout << "Starting..." << std::endl;

    // --- OPTIMIZED SECTION ---
    // std::sort typically uses IntroSort, which is O(n log n)
    std::sort(nums, nums + LISTSIZE);
    // -------------------------
    
    std::cout << "Done!" << std::endl;

    return 0;
}