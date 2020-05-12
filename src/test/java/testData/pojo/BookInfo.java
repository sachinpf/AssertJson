package testData.pojo;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import testData.pojo.random.RandomGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Data
public class BookInfo {
    private String bookName;
    private JsonArray category;
    private int publicationYear;
    private float bookPrice;
    private String sellingCurrency;
    private String website;


    public BookInfo(boolean generateDefault) {
        if (generateDefault) {
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
            sellingCurrency = "US-$";
            website = "https://www.amazonsite.com/book_id?=" + RandomGenerator.randomString(5);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookInfo)) return false;
        BookInfo bookInfo = (BookInfo) o;
        return getPublicationYear() == bookInfo.getPublicationYear() &&
                Float.compare(bookInfo.getBookPrice(), getBookPrice()) == 0 &&
                Objects.equals(getBookName(), bookInfo.getBookName()) &&
                Objects.equals(getCategory(), bookInfo.getCategory()) &&
                Objects.equals(getSellingCurrency(), bookInfo.getSellingCurrency()) &&
                Objects.equals(getWebsite(), bookInfo.getWebsite());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBookName(), getCategory(), getPublicationYear(), getBookPrice(), getSellingCurrency(), getWebsite());
    }

    public JsonObject toJsonObject() {
        JsonObject r = new JsonObject();
        r.addProperty("BookName", bookName);

        r.add("Category",  category);
        r.addProperty("PublicationYear", +publicationYear);
        r.addProperty("BookPrice", bookPrice);
        r.addProperty("SellingCurrency", sellingCurrency);
        r.addProperty("Website", website);
        return r;
    }
}
