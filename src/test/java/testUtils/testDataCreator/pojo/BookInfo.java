package testUtils.testDataCreator.pojo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import testUtils.testDataCreator.pojo.random.RandomGenerator;


public class BookInfo {
    private String bookName;
    private JsonArray category;
    private String category1;
    private int publicationYear;
    private String publicationYear1;
    private float bookPrice;
    private String bookPrice1;
    private String sellingCurrency;
    private String website;
    private Boolean alternate;

    public BookInfo() {

    }

    public BookInfo(boolean alternate) {
        this();
        this.alternate = alternate;

        if (alternate) {
            bookName = "Alchemist - " + RandomGenerator.randomString(5);
            category = new JsonArray();
            category.add("Fiction");
            category.add(RandomGenerator.randomString(8));
            category.add(RandomGenerator.randomString(6));
            category.add("Drama");
            category.add(RandomGenerator.randomString(3));
            category.add("Suspense");
            category.add(RandomGenerator.randomString(12));
            bookPrice = RandomGenerator.randomInt(2);
            publicationYear = RandomGenerator.randomInt(4);
        } else {
            bookName = "Story Book - " + RandomGenerator.randomString(5);
            category1 =
                    "[" + "Story,"
                            + RandomGenerator.randomString(8)
                            + ",Romance,"
                            + RandomGenerator.randomString(3)
                            + ",Suspense]";
            bookPrice1 = "" + RandomGenerator.randomInt(2);
            publicationYear1 = "" + RandomGenerator.randomInt(4);
        }

        sellingCurrency = "US-$";
        website = "https://www.site.com/book_id?=" + RandomGenerator.randomString(5);
    }

    public JsonObject toJsonObject() {
        JsonObject r = new JsonObject();
        r.addProperty("SellingCurrency", sellingCurrency);
        r.addProperty("Website", website);

        if (alternate) {
            r.addProperty("BookName", bookName);
            r.add("Category", category);
            r.addProperty("BookPrice", bookPrice);
            r.addProperty("PublicationYear", publicationYear);
        } else {
            r.addProperty("Book_Name", bookName);
            r.addProperty("Category", category1);
            r.addProperty("Book_Price", bookPrice1);
            r.addProperty("Publication_Year", publicationYear1);
        }
        return r;
    }

    public String getBookName() {
        return bookName;
    }

    public JsonArray getCategory() {
        return category;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public float getBookPrice() {
        return bookPrice;
    }

    public String getSellingCurrency() {
        return sellingCurrency;
    }

    public String getWebsite() {
        return website;
    }
}
