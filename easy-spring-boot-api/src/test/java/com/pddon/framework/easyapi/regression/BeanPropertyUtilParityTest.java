package com.pddon.framework.easyapi.regression;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pddon.framework.easyapi.annotation.IgnoreSign;
import com.pddon.framework.easyapi.lambda.DecorateContentFunction;
import com.pddon.framework.easyapi.utils.BeanPropertyUtil;

/**
 * 优化点1的对照测试：BeanPropertyUtil 旧实现（HEAD）与优化后实现的逐字段/逐值对照。
 */
public class BeanPropertyUtilParityTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void testObjToStringMapMatchesLegacyWithRichObject() {
        Map<String, String> legacy = LegacyBeanPropertyUtil.objToStringMap(newRoot(), "", IgnoreSign.class);
        Map<String, String> produced = BeanPropertyUtil.objToStringMap(newRoot(), "", IgnoreSign.class);

        Assert.assertEquals(legacy, produced);
        // @IgnoreSign 字段被忽略；无公共getter的字段不会进入结果；null字段不会进入结果
        Assert.assertFalse(produced.containsKey("ignoreMe"));
        Assert.assertFalse(produced.containsKey("noGetter"));
        Assert.assertFalse(produced.containsKey("nullField"));
        Assert.assertTrue(produced.containsKey("intValue"));
        Assert.assertTrue(produced.containsKey("created"));
        Assert.assertTrue(produced.containsKey("branch.flag"));
        Assert.assertTrue(produced.containsKey("items[0].name"));
        Assert.assertTrue(produced.containsKey("itemMap.first.name"));
        Assert.assertTrue(produced.containsKey("itemArray[1].name"));
        Assert.assertTrue(produced.containsKey("nested.name"));
    }

    @Test
    public void testObjToStringMapMatchesLegacyWithParentKeyAndNoIgnoreAnno() {
        Map<String, String> legacy = LegacyBeanPropertyUtil.objToStringMap(newRoot(), "req", null);
        Map<String, String> produced = BeanPropertyUtil.objToStringMap(newRoot(), "req", null);

        Assert.assertEquals(legacy, produced);
        Assert.assertTrue(produced.containsKey("req.ignoreMe"));
        Assert.assertTrue(produced.containsKey("req.branch.rootBack") == false);
        Assert.assertTrue(produced.containsKey("req.created"));
    }

    @Test
    public void testObjToStringMapMatchesLegacyForListAndMapRoot() {
        List<Item> list = new ArrayList<>();
        list.add(new Item("a", 1));
        list.add(new Item("b", 2));
        Map<String, Item> map = new LinkedHashMap<>();
        map.put("k1", new Item("m1", 11));
        map.put("k2", new Item("m2", 22));

        Assert.assertEquals(
                LegacyBeanPropertyUtil.objToStringMap(list, "list", null),
                BeanPropertyUtil.objToStringMap(list, "list", null));
        Assert.assertEquals(
                LegacyBeanPropertyUtil.objToStringMap(map, "map", null),
                BeanPropertyUtil.objToStringMap(map, "map", null));
    }

    @Test
    public void testObjToStringMapDoesNotMutateSourceSet() {
        Root root = newRoot();
        int before = root.getItemSet().size();
        BeanPropertyUtil.objToStringMap(root, "", IgnoreSign.class);
        Assert.assertEquals("Set分支不应清空调用方传入的集合", before, root.getItemSet().size());
        Assert.assertTrue(root.getItemSet().iterator().hasNext());
    }

    @Test
    public void testGetAllFieldMatchesLegacyIncludingHiddenParentField() {
        Assert.assertEquals(fieldSignatures(LegacyBeanPropertyUtil.getAllField(new Branch())),
                fieldSignatures(BeanPropertyUtil.getAllField(new Branch())));
        // 父子类同名字段都应保留
        String producedNames = fieldNames(BeanPropertyUtil.getAllField(new Branch()));
        Assert.assertTrue(producedNames.contains("flag"));
        Assert.assertTrue(producedNames.contains("parentField"));
    }

    @Test
    public void testDecorateObjMatchesLegacyOnAcyclicObject() throws Throwable {
        DecorateRoot legacyRoot = newDecorateRoot();
        DecorateRoot producedRoot = newDecorateRoot();
        List<String> legacyRecords = new ArrayList<>();
        List<String> producedRecords = new ArrayList<>();

        Object legacyResult = LegacyBeanPropertyUtil.decorateObj("root", legacyRoot,
                new LinkedHashMap<Class<?>, java.lang.annotation.Annotation>(), recorder(legacyRecords));
        Object producedResult = BeanPropertyUtil.decorateObj("root", producedRoot,
                new LinkedHashMap<Class<?>, java.lang.annotation.Annotation>(), recorder(producedRecords));

        String legacyJson = MAPPER.writeValueAsString(legacyResult);
        String producedJson = MAPPER.writeValueAsString(producedResult);
        Assert.assertEquals(legacyJson, producedJson);
        Assert.assertEquals(legacyRecords, producedRecords);
        Assert.assertTrue(legacyJson.contains("\"name\":\"name#ROOT\"") ||
                legacyJson.contains("\"name\":\"NAME#ROOT\""));
        Assert.assertTrue(producedJson.contains("\"name\":\"name#ROOT\"") ||
                producedJson.contains("\"name\":\"NAME#ROOT\""));
    }

    @Test
    public void testDecorateObjMatchesLegacyOnCircularObject() throws Throwable {
        Root legacyRoot = newRoot();
        Root producedRoot = newRoot();
        List<String> legacyRecords = new ArrayList<>();
        List<String> producedRecords = new ArrayList<>();

        Object legacyResult = LegacyBeanPropertyUtil.decorateObj("root", legacyRoot,
                new LinkedHashMap<Class<?>, java.lang.annotation.Annotation>(), recorder(legacyRecords));
        Object producedResult = BeanPropertyUtil.decorateObj("root", producedRoot,
                new LinkedHashMap<Class<?>, java.lang.annotation.Annotation>(), recorder(producedRecords));

        Assert.assertSame(legacyRoot, legacyResult);
        Assert.assertSame(producedRoot, producedResult);
        Assert.assertEquals(legacyRecords, producedRecords);
        // 循环引用未被破坏
        Assert.assertSame(producedRoot, producedRoot.getBranch().getRootBack());
        Assert.assertSame(legacyRoot, legacyRoot.getBranch().getRootBack());
        // 普通字段已完成处理
        Assert.assertTrue(producedRoot.getName().contains("name#ROOT"));
    }

    private static DecorateContentFunction recorder(final List<String> records) {
        return new DecorateContentFunction() {
            @Override
            public Object decorate(String fieldName, Object data, Map<Class<?>, java.lang.annotation.Annotation> annotations) {
                records.add(fieldName + "=" + describe(data));
                if (data instanceof String) {
                    return fieldName + "#" + ((String) data).toUpperCase();
                }
                return data;
            }
        };
    }

    private static String describe(Object data) {
        if (data == null) {
            return "null";
        }
        if (data instanceof String || data instanceof Number || data instanceof Boolean || data instanceof Character) {
            return data.toString();
        }
        return data.getClass().getSimpleName();
    }

    private static String fieldNames(List<java.lang.reflect.Field> fields) {
        StringBuilder sb = new StringBuilder();
        for (java.lang.reflect.Field field : fields) {
            sb.append(field.getName()).append(',');
        }
        return sb.toString();
    }

    private static List<String> fieldSignatures(List<java.lang.reflect.Field> fields) {
        List<String> signatures = new ArrayList<>();
        for (java.lang.reflect.Field field : fields) {
            signatures.add(field.getDeclaringClass().getName() + "#" + field.getName() + ":" + field.getType().getName());
        }
        return signatures;
    }

    private static DecorateRoot newDecorateRoot() {
        DecorateRoot root = new DecorateRoot();
        root.setName("root");
        root.setCount(8);
        root.setActive(true);
        root.setNullable(null);
        root.setNested(new DecorateItem("nested", 3));
        List<DecorateItem> items = new ArrayList<>();
        items.add(new DecorateItem("first", 1));
        items.add(new DecorateItem("second", 2));
        root.setItems(items);
        Map<String, DecorateItem> itemMap = new LinkedHashMap<>();
        itemMap.put("a", new DecorateItem("mapA", 10));
        itemMap.put("b", new DecorateItem("mapB", 20));
        root.setItemMap(itemMap);
        root.setItemArray(new DecorateItem[]{new DecorateItem("arr0", 100), new DecorateItem("arr1", 200)});
        return root;
    }

    private static Root newRoot() {
        Root root = new Root();
        root.setIntValue(7);
        root.setActive(true);
        root.setTotal(100L);
        root.setCreated(new Date(1600000000000L));
        root.setName("root");
        root.setNullField(null);
        root.setKind(Kind.A);
        root.setAmount(new BigDecimal("12.34"));
        root.setNested(new Item("nested", 3));
        List<Item> items = new ArrayList<>();
        items.add(new Item("l1", 1));
        items.add(new Item("l2", 2));
        root.setItems(items);
        Map<String, Item> itemMap = new LinkedHashMap<>();
        itemMap.put("first", new Item("m1", 11));
        itemMap.put("second", new Item("m2", 22));
        root.setItemMap(itemMap);
        Set<Item> itemSet = new LinkedHashSet<>();
        itemSet.add(new Item("s1", 5));
        itemSet.add(new Item("s2", 6));
        root.setItemSet(itemSet);
        root.setItemArray(new Item[]{new Item("a0", 30), new Item("a1", 31)});
        Branch branch = new Branch();
        branch.setRootBack(root);
        root.setBranch(branch);
        return root;
    }

    public enum Kind {
        A, B
    }

    public static class Item implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private int qty;

        public Item() {
        }

        Item(String name, int qty) {
            this.name = name;
            this.qty = qty;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getQty() {
            return qty;
        }

        public void setQty(int qty) {
            this.qty = qty;
        }
    }

    public static class ParentBean implements Serializable {
        private static final long serialVersionUID = 1L;
        private boolean flag = false;
        private String parentField = "parent-field";

        public boolean isFlag() {
            return flag;
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }

        public String getParentField() {
            return parentField;
        }

        public void setParentField(String parentField) {
            this.parentField = parentField;
        }
    }

    public static class Branch extends ParentBean {
        private static final long serialVersionUID = 1L;
        private Boolean flag = Boolean.TRUE;
        private String label = "branch";
        private Root rootBack;

        public Boolean getFlag() {
            return flag;
        }

        public void setFlag(Boolean flag) {
            this.flag = flag;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Root getRootBack() {
            return rootBack;
        }

        public void setRootBack(Root rootBack) {
            this.rootBack = rootBack;
        }
    }

    public static class Root implements Serializable {
        private static final long serialVersionUID = 1L;
        private int intValue;
        private boolean active;
        private Long total;
        private Date created;
        private String name;
        private String nullField;
        private Kind kind;
        private BigDecimal amount;
        private Item nested;
        private List<Item> items;
        private Map<String, Item> itemMap;
        private Set<Item> itemSet;
        private Item[] itemArray;
        private Branch branch;
        @IgnoreSign
        private String ignoreMe = "ignore-me";
        private String noGetter = "no-getter";

        public int getIntValue() {
            return intValue;
        }

        public void setIntValue(int intValue) {
            this.intValue = intValue;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public Long getTotal() {
            return total;
        }

        public void setTotal(Long total) {
            this.total = total;
        }

        public Date getCreated() {
            return created;
        }

        public void setCreated(Date created) {
            this.created = created;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNullField() {
            return nullField;
        }

        public void setNullField(String nullField) {
            this.nullField = nullField;
        }

        public Kind getKind() {
            return kind;
        }

        public void setKind(Kind kind) {
            this.kind = kind;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public Item getNested() {
            return nested;
        }

        public void setNested(Item nested) {
            this.nested = nested;
        }

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }

        public Map<String, Item> getItemMap() {
            return itemMap;
        }

        public void setItemMap(Map<String, Item> itemMap) {
            this.itemMap = itemMap;
        }

        public Set<Item> getItemSet() {
            return itemSet;
        }

        public void setItemSet(Set<Item> itemSet) {
            this.itemSet = itemSet;
        }

        public Item[] getItemArray() {
            return itemArray;
        }

        public void setItemArray(Item[] itemArray) {
            this.itemArray = itemArray;
        }

        public Branch getBranch() {
            return branch;
        }

        public void setBranch(Branch branch) {
            this.branch = branch;
        }

        public String getIgnoreMe() {
            return ignoreMe;
        }

        public void setIgnoreMe(String ignoreMe) {
            this.ignoreMe = ignoreMe;
        }
    }

    public static class DecorateItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private int qty;

        public DecorateItem() {
        }

        DecorateItem(String name, int qty) {
            this.name = name;
            this.qty = qty;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getQty() {
            return qty;
        }

        public void setQty(int qty) {
            this.qty = qty;
        }
    }

    public static class DecorateRoot implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private Integer count;
        private boolean active;
        private String nullable;
        private DecorateItem nested;
        private List<DecorateItem> items;
        private Map<String, DecorateItem> itemMap;
        private DecorateItem[] itemArray;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public String getNullable() {
            return nullable;
        }

        public void setNullable(String nullable) {
            this.nullable = nullable;
        }

        public DecorateItem getNested() {
            return nested;
        }

        public void setNested(DecorateItem nested) {
            this.nested = nested;
        }

        public List<DecorateItem> getItems() {
            return items;
        }

        public void setItems(List<DecorateItem> items) {
            this.items = items;
        }

        public Map<String, DecorateItem> getItemMap() {
            return itemMap;
        }

        public void setItemMap(Map<String, DecorateItem> itemMap) {
            this.itemMap = itemMap;
        }

        public DecorateItem[] getItemArray() {
            return itemArray;
        }

        public void setItemArray(DecorateItem[] itemArray) {
            this.itemArray = itemArray;
        }
    }
}
