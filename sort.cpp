
#include <iostream>
#include <vector>
#include <random>
#include <chrono>
#include <array>
#include <cstdint>
#include <cstring>
#include <type_traits>
#include <algorithm>

int main() {
    //  generate working arrays to test
    std::vector<std::vector<int>> bigBatch;
    bigBatch.reserve(100);

    for(auto& v : bigBatch) {
    v = generateArray();
    }

    //  setup and run search algorithm methods 100 times to calculate averae time
    std::vector<double> time;
    time.reserve(100);

    for(auto& v : bigBatch) {
        auto start = std::chrono::high_resolution_clock::now();
        //  PUT SEARCH FUNCTION HERE WITH INCRIMENTING VECOTR PASSES INTO IT DO THIS FOR EACH FUNCTION!
        gptSort(v);
        auto end = std::chrono::high_resolution_clock::now();
        std::chrono::duration<double> elapsed = end - start;
        time.push_back(elapsed.count());
    }
    return 0;
}

std::vector<int> generateArray() {
    
    std::vector<int> batch;
    batch.reserve(100000);

    //  setup random (a pain in c)
    std::random_device rd;
    std::mt19937_64 gen(rd);
    std::uniform_int_distribution<> dist(0,2147483647);

    for(auto& v : batch) {
        batch.push_back(dist(gen));
    }
    return batch;
}

//  algorithm functions (human, codex, gemini, claud)
void gptSort(std::vector<int> ary) {
    static_assert(sizeof(int) == 4, "This radix sort requires 32-bit int.");

    const size_t n = ary.size();
    if (n <= 1) return;

    std::vector<int> tmp(n);

    // We flip the sign bit so signed ints sort correctly as unsigned.
    auto key = [](int x) -> uint32_t {
        return static_cast<uint32_t>(x) ^ 0x80000000u;
    };

    constexpr int RADIX = 256;
    std::array<size_t, RADIX> count;

    std::vector<int>* src = &ary;
    std::vector<int>* dst = &tmp;

    for (int shift = 0; shift < 32; shift += 8) {
        count.fill(0);

        // Count byte frequencies
        for (size_t i = 0; i < n; ++i) {
            ++count[(key((*src)[i]) >> shift) & 0xFF];
        }

        // Prefix sums
        size_t sum = 0;
        for (int i = 0; i < RADIX; ++i) {
            size_t c = count[i];
            count[i] = sum;
            sum += c;
        }

        // Stable scatter
        for (size_t i = 0; i < n; ++i) {
            int v = (*src)[i];
            (*dst)[count[(key(v) >> shift) & 0xFF]++] = v;
        }

        std::swap(src, dst);
    }

    // After 4 passes, result may be in tmp
    if (src != &ary) {
        ary.swap(*src);
    }
}
void gemeniSort(std::vector<int> ary) {std::sort(ary.begin(), ary.end());}
void claudSort(std::vector<int> ary) {std::sort(ary.begin(), ary.end());}
void humanSort(std::vector<int> ary) {}