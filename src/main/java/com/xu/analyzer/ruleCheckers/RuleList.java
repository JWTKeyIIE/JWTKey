package com.xu.analyzer.ruleCheckers;

import java.util.ArrayList;

public enum RuleList {
    ShortSymmetricKey(1, new Integer[] {},"Use Short Symmetric Key"),
    ConstantSymmetricKey(2, new Integer[] {}, "Use constant Symmetric Key"),
    HardCodedSymmetricKey(3, new Integer[] {}, "HardCoded Symmetric Key"),
    PropertySymmetricKey(4, new Integer[] {},"Use Property File Store Symmetric Key"),
    ShortAsymmetricKey(5, new Integer[] {}, "Use 1024 bits RSA key"),
    HardCodedAsymmetricKey(6, new Integer[] {}, "HardCoded Asymmetric Key"),
    HTTP(7, new Integer[] {650}, "Used HTTP Protocol"),
    UnsecureCertVerify(8,new Integer[] {},"Certificate verify problem"),
    UnsecureAPIUsage(9, new Integer[] {}, "Jwt Library API misused"),
    UNCREATEDRULE(-1, new Integer[] {-1}, "Used as a placeholder as the default search value");
    //endregion

    //region Attributes
    private Integer ruleId;
    private String desc;
    private Integer[] cweId;
    //endregion

    //region Constructor
    RuleList(Integer ruleId, Integer[] cweId, String desc) {
        this.ruleId = ruleId;
        this.desc = desc;
        this.cweId = cweId;
    }
    //endregion

    //region Getters

    /**
     * getRuleByRuleNumber.
     *
     * @param ruleNumber a {@link Integer} object.
     * @return a {@link RuleList} object.
     */
    public static RuleList getRuleByRuleNumber(Integer ruleNumber) {
        for (RuleList rule : RuleList.values())
            if (rule.getRuleId().equals(ruleNumber)) {
                return rule;
            }
        return RuleList.UNCREATEDRULE;
    }

    /**
     * Getter for the field <code>ruleId</code>.
     *
     * @return a {@link Integer} object.
     */
    public Integer getRuleId() {
        return ruleId;
    }

    /**
     * Getter for the field <code>desc</code>.
     *
     * @return a {@link String} object.
     */
    public String getDesc() {
        return desc;
    }
    //endregion

    //region Accessors

    /**
     * Getter for cweId
     *
     * <p>getCweId()
     *
     * @return {@link Integer[]} - The cweId.
     */
    public Integer[] getCweId() {
        return cweId;
    }

    /**
     * retrieveCWEInfo({@link CWEList})
     *
     * @param list a {@link CWEList} object.
     * @return {@link ArrayList} object.
     */
/*    public ArrayList<CWE> retrieveCWEInfo(CWEList list) {
        ArrayList<CWE> out = new ArrayList<>();

        for (Integer cweId : this.cweId) out.add(list.CWE_Lookup(cweId));

        return out;
    }*/

}
