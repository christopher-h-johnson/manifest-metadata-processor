/*
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
package de.ubleipzig.metadata.producer.doc;

import org.xmlbeam.annotation.XBRead;

import java.util.List;
import java.util.Optional;

/**
 * MetsData.
 *
 * @author christopher-johnson
 */
public interface MetsData {

    /**
     * @return String
     */
    @XBRead("//*[local-name()='mods']/*[local-name()='titleInfo']/*[local-name()='title']")
    Optional<String> getManifestTitle();

    /**
     * @return String
     */
    @XBRead("//*[local-name()='owner']")
    Optional<String> getAttribution();

    /**
     * @return String
     */
    @XBRead("//*[local-name()='presentation']")
    Optional<String> getPresentationUri();

    /**
     * @return String
     */
    @XBRead("//*[local-name()='ownerLogo']")
    Optional<String> getLogo();

    /**
     * @return String
     */
    @XBRead("//*[local-name()='typeOfResource'][@manuscript='yes']")
    Optional<String> getManuscriptType();

    /**
     * @param idType String
     * @return String
     */
    @XBRead("//*[local-name()='identifier'][@type='{0}']")
    Optional<String> getManuscriptIdByType(String idType);

    /**
     * @return Optional
     */
    @XBRead("//*[local-name()='subtitle']")
    Optional<String> getSubtitle();

    /**
     * @return Optional
     */
    @XBRead("//*[local-name()='typeOfResource']")
    Optional<String> getMedium();

    /**
     * @return Optional
     */
    @XBRead("//*[local-name()='form'][@type='material']")
    Optional<String> getMaterial();

    /**
     * @return Optional
     */
    @XBRead("//*[local-name()='extent'][@unit='leaves']")
    Optional<String> getExtent();

    /**
     * @return Optional
     */
    @XBRead("//*[local-name()='extent'][@unit='cm']")
    Optional<String> getDimension();

    /**
     * @return Optional
     */
    @XBRead("//*[local-name()='language']")
    Optional<String> getLanguage();

    /**
     * @return Optional
     */
    @XBRead("//*[local-name()='place'][@eventType='manufacture']")
    Optional<String> getLocation();

    /**
     * @return Optional
     */
    @XBRead("//*[local-name()='recordIdentifier']")
    Optional<String> getRecordIdentifier();

    /**
     * @return Optional
     */
    @XBRead("//*[local-name()='dateCreated']")
    Optional<String> getDateCreated();

    /**
     * @return String
     */
    @XBRead("//*[local-name()='note']")
    Optional<String> getNote();

    /**
     * @return List
     */
    @XBRead("//*[local-name()='note']/@type")
    List<String> getNoteTypes();

    /**
     * @param noteType String
     * @return String
     */
    @XBRead("//*[local-name()='note'][@type='{0}']")
    Optional<String> getNotesByType(String noteType);

    /**
     * @return String
     */
    @XBRead("//*[local-name()='number']")
    Optional<String> getCensus();

    /**
     * @return String
     */
    @XBRead("//*[local-name()='title']")
    Optional<String> getCollection();

    /**
     * @return String
     */
    @XBRead("//*[local-name()='shelfLocator']")
    Optional<String> getCallNumber();

    /**
     * @return String
     */
    @XBRead("//*[local-name()='owner']")
    Optional<String> getOwner();

    /**
     * @return String
     */
    @XBRead("//*[local-name()='displayForm']")
    Optional<String> getAuthor();

    /**
     * @return String
     */
    @XBRead("//*[local-name()='placeTerm']")
    Optional<String> getPlace();

    /**
     * @return String
     */
    @XBRead("//*[local-name()='dateOther']")
    Optional<String> getDate();

    /**
     * @return String
     */
    @XBRead("//*[local-name()='publisher']")
    Optional<String> getPublisher();

    /**
     * @return String
     */
    @XBRead("//*[local-name()='extent']")
    Optional<String> getPhysState();

    /**
     * @return List
     */
    @XBRead("//*[local-name()='structMap'][@TYPE='PHYSICAL']/*[local-name()" + "='div']/descendant::node()/@ID")
    List<String> getPhysicalDivs();

    /**
     * @param div String
     * @return String
     */
    @XBRead("//*[local-name()='div'][@ID='{0}']/@ORDERLABEL")
    Optional<String> getOrderLabelForDiv(String div);

    /**
     * @param div String
     * @return String
     */
    @XBRead("//*[local-name()='div'][@ID='{0}']/descendant::node()/@FILEID")
    Optional<String> getFileIdForDiv(String div);

    /**
     * @param file String
     * @return String
     */
    @XBRead("//*[local-name()='file'][@ID='{0}']/descendant::node()/@*[local-name()='href']")
    Optional<String> getHrefForFile(String file);

    /**
     * @param file String
     * @return String
     */
    @XBRead("//*[local-name()='file'][@ID='{0}']/@MIMETYPE")
    Optional<String> getMimeTypeForFile(String file);

    /**
     * @return List
     */
    @XBRead("//*[local-name()='structLink']/*[local-name()='smLink']")
    List<Xlink> getXlinks();

    /**
     * @param id String
     * @return Logical
     */
    @XBRead("//*[local-name()='structMap'][@TYPE='LOGICAL']//*[local-name()" + "='div'][@ID='{0}']/*[local-name()"
            + "='div'][last()]")
    Logical getLogicalLastDescendent(String id);

    /**
     * @return List
     */
    @XBRead("//*[local-name()='structMap'][@TYPE='LOGICAL']/*[local-name()" + "='div']/*[local-name()='div'][not"
            + "(descendant::div)]")
    List<Logical> getTopLogicals();

    /**
     * @param id String
     * @return List
     */
    @XBRead("//*[local-name()='div'][@ID='{0}']/parent::node()")
    List<Logical> getLogicalLastParent(String id);

    /**
     * @param id String
     * @return List
     */
    @XBRead("//*[local-name()='div'][@ID='{0}']/*[local-name()='div']")
    List<Logical> getLogicalLastChildren(String id);

    /**
     * @param id String
     * @return Optional
     */
    @XBRead("//*[local-name()='structMap'][@TYPE='LOGICAL']//*[local-name()" + "='div'][@ID='{0}']/@LABEL")
    Optional<String> getLogicalLabel(String id);

    /**
     * @param id String
     * @return Optional
     */
    @XBRead("//*[local-name()='structMap'][@TYPE='LOGICAL']//*[local-name()" + "='div'][@ID='{0}']/@TYPE")
    Optional<String> getLogicalType(String id);

    interface Xlink {

        @XBRead("@*[local-name()='from']")
        String getXLinkFrom();

        @XBRead("@*[local-name()='to']")
        String getXLinkTo();
    }

    interface Logical {

        @XBRead("@*[local-name()='ID']")
        String getLogicalId();

        @XBRead("@*[local-name()='LABEL']")
        String getLogicalLabel();

        @XBRead("@*[local-name()='TYPE']")
        String getLogicalType();
    }
}
