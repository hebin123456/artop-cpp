Required manual modifications after regeneration of Hummingbird 2.0 metamodel:
* Common20XMI.xsd:
  - xsd:complexType element for Identifiable: Add mixed="true" attribute
  - xsd:complexType element for Description: Add mixed="true" attribute
  - xsd:simpleType element for LanguageCultureName: Replace values of value="xxYY" attributes on xsd:enumeration elements
    nested into contained xsd:restriction element with "xx-YY"
* TypeModel20XMI.xsd:
  - schemaLocation attribute of xsd:import element: Change value to Common20XMI.xsd
  - xsd:complexType element for Platform: Add mixed="true" attribute to contained xsd:complexContent element
  - xsd:complexType element for ComponentType: Add mixed="true" attribute to contained xsd:complexContent element
  - xsd:complexType element for Port: Add mixed="true" attribute to contained xsd:complexContent element
  - xsd:complexType element for Interface: Add mixed="true" attribute to contained xsd:complexContent element
  - xsd:complexType element for Parameter: Add mixed="true" attribute to contained xsd:complexContent element
* InstanceModel20XMI.xsd:
  - schemaLocation attributes of xsd:import elements: Change values to TypeModel20XMI.xsd and Common20XMI.xsd
  - xsd:complexType element for Application: Add mixed="true" attribute to contained xsd:complexContent element
  - xsd:complexType element for Component: Add mixed="true" attribute to contained xsd:complexContent element
  - xsd:complexType element for Connection: Add mixed="true" attribute to contained xsd:complexContent element
  - xsd:complexType element for ParameterValue: Add mixed="true" attribute to contained xsd:complexContent element
  - xsd:complexType element for ParameterExpression:
    o Add mixed="true" attribute
    o Replace ref="xmi:Extension" attribute on xsd:element element nested into contained xsd:choice element
      with name="expressions" type="im:Formula" attributes
