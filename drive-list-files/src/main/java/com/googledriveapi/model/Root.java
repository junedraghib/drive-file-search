package com.googledriveapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Root implements Serializable {
    private static final long serialVersionUID = 1L;
    public ArrayList<Page> pages;
    public ExtendedMetadata extended_metadata;
    public ArrayList<Element> elements;
    public Version version;

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Page{
        public Boxes boxes;
        public int page_number;
        public int rotation;
        public boolean is_scanned;
        public int width;
        public int height;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Boxes{
        @JsonProperty("CropBox")
        public ArrayList<Integer> cropBox;
        @JsonProperty("MediaBox")
        public ArrayList<Integer> mediaBox;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Version{
        public String schema;
        public String table_structure;
        public String page_segmentation;
        public String json_export;
        public String structure;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Element{
        @JsonProperty("Path")
        public String path;
        @JsonProperty("HasClip")
        public boolean hasClip;
        @JsonProperty("Bounds")
        public ArrayList<Double> bounds;
        @JsonProperty("TextSize")
        public double textSize;
        @JsonProperty("Lang")
        public String lang;
        @JsonProperty("Page")
        public int page;
        @JsonProperty("Text")
        public String text;
        public Attributes attributes;
        @JsonProperty("Font")
        public Font font;
        @JsonProperty("ClipBounds")
        public ArrayList<Double> clipBounds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Font{
        public String name;
        public int weight;
        public String alt_family_name;
        public boolean monospaced;
        public String encoding;
        public boolean embedded;
        public String family_name;
        public String font_type;
        public boolean italic;
        public boolean subset;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attributes{
        @JsonProperty("SpaceAfter")
        public double spaceAfter;
        @JsonProperty("Placement")
        public String placement;
        @JsonProperty("BBox")
        public ArrayList<Double> bBox;
        @JsonProperty("LineHeight")
        public double lineHeight;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtendedMetadata{
        public boolean is_encrypted;
        public boolean is_certified;
        @JsonProperty("ID_permanent")
        public String iD_permanent;
        public String language;
        public String pdf_version;
        @JsonProperty("ID_instance")
        public String iD_instance;
        public boolean has_acroform;
        public boolean is_digitally_signed;
        public boolean has_embedded_files;
        public boolean is_XFA;
        public String pdfa_compliance_level;
        public String pdfua_compliance_level;
        public int page_count;
    }
}


