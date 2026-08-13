// Diagnostic: time match vs diff vs equivalences, with/without provider
#include "emf/artop/runtime/AutosarResource.h"
#include "emf/artop/runtime/AutosarResourceFactory.h"
#include "emf/artop/runtime/AutosarReleaseDescriptor.h"
#include "emf/artop/runtime/IdentifiableUtil.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIOptions.h"
#include "emf/common/EObject.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/compare/Comparison.h"
#include "emf/compare/MatchEngine.h"
#include "emf/compare/DiffEngine.h"

#include <chrono>
#include <cstdio>
#include <fstream>
#include <iostream>
#include <memory>
#include <string>

#include "autosar40/autosartoplevelstructure/AutosartoplevelstructurePackage.h"

static std::shared_ptr<emf::artop::runtime::AutosarXMLResource> loadResource(const std::string& path) {
    emf::common::URI uri("file:" + path);
    auto res = std::make_shared<emf::artop::runtime::AutosarXMLResource>(uri);
    res->setSchemaLocation("http://autosar.org/schema/r4.0 AUTOSAR_00048.xsd");
    emf::xmi::XMIOptions opts;
    std::ifstream ifs(path, std::ios::binary);
    res->load(ifs, opts);
    return res;
}

using Clock = std::chrono::high_resolution_clock;
static double ms(Clock::time_point a, Clock::time_point b) {
    return std::chrono::duration<double, std::milli>(b - a).count();
}

int main(int argc, char** argv) {
    std::string inputFile = (argc > 1) ? argv[1]
        : "/workspace/java/demo/output/ECUConfigurationParameters.arxml";

    extern void init_all_autosar40_packages();
    init_all_autosar40_packages();
    emf::common::EPackageRegistry::instance().put(
        "http://autosar.org/schema/r4.0",
        emf::artop::autosar40::autosartoplevelstructure::AutosartoplevelstructurePackage::eINSTANCE);

    std::cout << "=== Compare Diagnostic ===" << std::endl;
    std::cout << "File: " << inputFile << std::endl;

    auto t0 = Clock::now();
    auto left = loadResource(inputFile);
    auto t0b = Clock::now();
    std::printf("Load left: %.0f ms\n", ms(t0, t0b));
    auto right = loadResource(inputFile);
    auto t1 = Clock::now();
    std::printf("Load right: %.0f ms\n", ms(t0b, t1));
    std::fflush(stdout);

    emf::common::EObject* leftRoot = left->getContents().empty() ? nullptr : left->getContents()[0];
    emf::common::EObject* rightRoot = right->getContents().empty() ? nullptr : right->getContents()[0];

    // Phase 1: match WITHOUT provider
    {
        emf::compare::Comparison comp;
        emf::compare::MatchEngine me;
        std::printf("Starting match (NO provider)...\n"); std::fflush(stdout);
        auto a = Clock::now();
        me.match(leftRoot, rightRoot, comp);
        auto b = Clock::now();
        std::printf("Match (NO provider):  %.0f ms  matches=%zu\n", ms(a, b), comp.getMatches().size());
        std::fflush(stdout);

        std::printf("Starting diff (NO provider)...\n"); std::fflush(stdout);
        auto c = Clock::now();
        emf::compare::DiffEngine de;
        de.diff(comp);
        auto d = Clock::now();
        std::printf("Diff  (NO provider):  %.0f ms\n", ms(c, d));
        std::fflush(stdout);
    }

    // Phase 2: match WITH provider
    {
        emf::compare::Comparison comp;
        emf::compare::MatchEngine me;
        me.setIdentifierProvider(emf::artop::runtime::IdentifiableUtil::asIdentifierProvider());
        std::printf("Starting match (W/ provider)...\n"); std::fflush(stdout);
        auto a = Clock::now();
        me.match(leftRoot, rightRoot, comp);
        auto b = Clock::now();
        std::printf("Match (W/ provider):  %.0f ms  matches=%zu\n", ms(a, b), comp.getMatches().size());
        std::fflush(stdout);

        std::printf("Starting diff (W/ provider)...\n"); std::fflush(stdout);
        auto c = Clock::now();
        emf::compare::DiffEngine de;
        de.diff(comp);
        auto d = Clock::now();
        std::printf("Diff  (W/ provider):  %.0f ms\n", ms(c, d));
        std::fflush(stdout);
    }

    std::cout << "=== DONE ===" << std::endl;
    return 0;
}
