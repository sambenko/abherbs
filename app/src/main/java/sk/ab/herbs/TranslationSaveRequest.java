package sk.ab.herbs;

import java.util.List;

/**
 * Created by adrian on 1.9.2015.
 */
public class TranslationSaveRequest {
    private String translationId;
    private Integer plantId;
    private String language;
    private List<String> texts;

    public TranslationSaveRequest(Integer plantId, String language, List<String> texts) {
        this.translationId = plantId.toString() + "_" + language;
        this.plantId = plantId;
        this.language = language;
        this.texts = texts;
    }

    public String getTranslationId() {
        return translationId;
    }

    public List<String> getTexts() {
        return texts;
    }
}
