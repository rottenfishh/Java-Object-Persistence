package nsu.sd;

import nsu.sd.metadata.ClassMetadata;

public class Main {
    public static void main(String[] args) {
        Blob blob = new Blob();
        blob.name = "Blob";
        blob.surname = "Blobov";
        blob.age = 5;

        MetadataRegistry registry = new MetadataRegistry();
        ClassMetadata md = registry.getClassMetadata(blob);
        System.out.println(md.getName());

    }
}