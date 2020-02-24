/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.gradle;

import java.io.BufferedWriter;
import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.AddOnCollection;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

public class GenerateWebsiteAddonsData extends DefaultTask {

    DumpSettings settings = DumpSettings.builder().build();
    Dump dump = new Dump(settings);

    private final RegularFileProperty zapVersions;
    private final RegularFileProperty into;

    public GenerateWebsiteAddonsData() {
        ObjectFactory objects = getProject().getObjects();
        this.zapVersions = objects.fileProperty();
        this.into = objects.fileProperty();

        setGroup("ZAP");
        setDescription("Created the zap addons yaml file for website");
    }

    @OutputFile
    public RegularFileProperty getInto() {
        return into;
    }

    @InputFile
    public RegularFileProperty getZapVersions() {
        return zapVersions;
    }

    @TaskAction
    public void update() throws Exception {
        File xmlFile = zapVersions.get().getAsFile();
        if (xmlFile.exists()) {
            Map<String, String> addOnData;
            List<Map<String, String>> addOnList = new ArrayList<>();
            AddOnCollection aoc =
                    new AddOnCollection(
                            new ZapXmlConfiguration(zapVersions.get().getAsFile()),
                            AddOnCollection.Platform.linux);
            for (AddOn addOn : aoc.getAddOns()) {
                addOnData = new HashMap<>();
                addOnData.put("id", addOn.getId());
                addOnData.put("name", addOn.getName());
                addOnData.put("description", addOn.getDescription());
                addOnData.put("author", addOn.getAuthor());
                addOnData.put("version", addOn.getVersion().toString());
                addOnData.put("file", addOn.getFile().getName());
                addOnData.put("status", addOn.getStatus().name());
                addOnData.put("url", addOn.getUrl().toString());
                addOnData.put("date", "");
                addOnData.put("infoUrl", getURL(addOn.getInfo()));
                addOnData.put("downloadUrl", addOn.getUrl().toString());
                addOnData.put("repoUrl", "");
                addOnData.put("size", String.valueOf(addOn.getSize()));
                addOnList.add(addOnData);
            }

            String output = dump.dumpToString(addOnList);

            try (BufferedWriter writer =
                    Files.newBufferedWriter(
                            into.get().getAsFile().toPath(), Charset.defaultCharset())) {
                writer.write(output);
            }
        } else {
            System.out.print("File not found!");
        }
    }

    public String getURL(URL url) {
        if (null != url) {
            return url.toString();
        }
        return "";
    }
}
