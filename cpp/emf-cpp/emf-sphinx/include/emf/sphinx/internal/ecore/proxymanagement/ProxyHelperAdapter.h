// ProxyHelperAdapter.h
// 对齐 Java org.eclipse.sphinx.emf.internal.ecore.proxymanagement.ProxyHelperAdapter
#pragma once

#include "emf/sphinx/internal/ecore/proxymanagement/ProxyHelper.h"

namespace emf::sphinx::internal::ecore::proxymanagement {

class ProxyHelperAdapter : public ProxyHelper {
public:
    ProxyHelperAdapter() = default;
    ~ProxyHelperAdapter() override = default;
};

}  // namespace emf::sphinx::internal::ecore::proxymanagement
