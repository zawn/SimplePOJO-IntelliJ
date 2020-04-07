package uk.me.jeffsutton.pojogen;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.sun.codemodel.JCodeModel;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.rules.RuleFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by jeff on 15/12/2015.
 */
class JPG {

    public static void generate(String src, File dest, String pkg, String main, Annotator annotator, SourceGenerationConfig config) throws IOException {
        JCodeModel codeModel = new JCodeModel();
        System.out.println("Using URL: " + src.toString());
        File tempFile = genTempFile();
        FileUtils.writeStringToFile(tempFile, src, "UTF-8");

        RuleFactory ruleFactory = new RuleFactory(config, annotator, new SchemaStore());
        SchemaMapper gen = new SchemaMapper(ruleFactory, new SchemaGenerator());
        gen.generate(codeModel, main, pkg, tempFile.toURI().toURL());
        System.out.println("Using file: " + dest.getAbsolutePath() + " " + dest.exists() + " " + dest.isDirectory());
        System.out.println("Using package: " + pkg);
        if (!dest.exists()) {
            dest.mkdir();
        }
        codeModel.build(dest);

        VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(dest);
        if (virtualFile != null) {
            virtualFile.refresh(false, true);
        }
        tempFile.deleteOnExit();
    }

    @NotNull
    private static File genTempFile() throws IOException {
        File dir = new File(FileUtils.getTempDirectory(), "JsonSchema2Pojo");
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
        dir.mkdirs();
        return File.createTempFile("jgen", "json", dir);
    }
}
