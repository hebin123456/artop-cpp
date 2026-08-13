#pragma once
#include <string>
namespace emf::ecore {
class Enumerator {
public:
    std::string name;
    std::string literal;
    int value = 0;
    Enumerator() = default;
    Enumerator(std::string n, std::string l, int v) : name(std::move(n)), literal(std::move(l)), value(v) {}
};
}
