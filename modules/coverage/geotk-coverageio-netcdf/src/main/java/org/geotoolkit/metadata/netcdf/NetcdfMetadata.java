/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2011-2012, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2011-2012, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.metadata.netcdf;

import java.util.Locale;
import java.util.logging.LogRecord;

import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.CF;

import org.opengis.metadata.Metadata;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.*;
import org.opengis.metadata.content.*;
import org.opengis.metadata.distribution.Distributor;
import org.opengis.metadata.distribution.Distribution;
import org.opengis.metadata.constraint.LegalConstraints;
import org.opengis.metadata.constraint.Restriction;
import org.opengis.metadata.spatial.GridSpatialRepresentation;
import org.opengis.metadata.spatial.SpatialRepresentationType;
import org.opengis.metadata.identification.DataIdentification;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.identification.KeywordType;
import org.opengis.metadata.identification.Keywords;
import org.opengis.metadata.quality.DataQuality;
import org.opengis.metadata.lineage.Lineage;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.VerticalExtent;
import org.opengis.metadata.extent.TemporalExtent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicDescription;

import org.geotoolkit.image.io.WarningProducer;


/**
 * Mapping from/to NetCDF metadata to ISO 19115-2 metadata.
 * This class defines the names of attributes used by the reader and writer sub-classes.
 * The NetCDF attributes defined by this class are:
 * <p>
 * {@value #ACCESS_CONSTRAINT}, {@value #ACKNOWLEDGMENT}, {@value #COMMENT},
 * {@linkplain #CONTRIBUTOR "contributor_email"},
 * {@linkplain #CONTRIBUTOR "contributor_name"},
 * {@linkplain #CONTRIBUTOR "contributor_role"},
 * {@linkplain #CONTRIBUTOR "contributor_url"},
 * {@linkplain #CREATOR     "creator_email"},
 * {@linkplain #CREATOR     "creator_name"},
 * {@linkplain #CREATOR     "creator_url"},
 * {@value #DATA_TYPE}, {@value #DATE_CREATED}, {@value #DATE_ISSUED}, {@value #DATE_MODIFIED},
 * {@value #FLAG_MASKS}, {@value #FLAG_MEANINGS}, {@value #FLAG_NAMES}, {@value #FLAG_VALUES},
 * {@linkplain #TITLE "full_name"},
 * {@linkplain #GEOGRAPHIC_IDENTIFIER "geographic_identifier"},
 * {@linkplain #LATITUDE  "geospatial_lat_max"},
 * {@linkplain #LATITUDE  "geospatial_lat_min"},
 * {@linkplain #LATITUDE  "geospatial_lat_resolution"},
 * {@linkplain #LATITUDE  "geospatial_lat_units"},
 * {@linkplain #LONGITUDE "geospatial_lon_max"},
 * {@linkplain #LONGITUDE "geospatial_lon_min"},
 * {@linkplain #LONGITUDE "geospatial_lon_resolution"},
 * {@linkplain #LONGITUDE "geospatial_lon_units"},
 * {@linkplain #VERTICAL  "geospatial_vertical_max"},
 * {@linkplain #VERTICAL  "geospatial_vertical_min"},
 * {@linkplain #VERTICAL  "geospatial_vertical_positive"},
 * {@linkplain #VERTICAL  "geospatial_vertical_resolution"},
 * {@linkplain #VERTICAL  "geospatial_vertical_units"},
 * {@value #HISTORY}, {@value #IDENTIFIER}, {@linkplain #CREATOR "institution"}, {@value #KEYWORDS},
 * {@value #LICENSE}, {@value #METADATA_CREATION}, {@linkplain #TITLE "name"}, {@value #NAMING_AUTHORITY},
 * {@value #PROCESSING_LEVEL}, {@value #PROJECT},
 * {@linkplain #PUBLISHER "publisher_email"},
 * {@linkplain #PUBLISHER "publisher_name"},
 * {@linkplain #PUBLISHER "publisher_url"},
 * {@value #PURPOSE}, {@value #REFERENCES}, {@value #STANDARD_NAME},
 * {@value #STANDARD_NAME_VOCABULARY}, {@value #SUMMARY},
 * {@linkplain #TIME "time_coverage_duration"},
 * {@linkplain #TIME "time_coverage_end"},
 * {@linkplain #TIME "time_coverage_resolution"},
 * {@linkplain #TIME "time_coverage_start"},
 * {@linkplain #TIME "time_coverage_units"},
 * {@value #TITLE}, {@value #TOPIC_CATEGORY} and {@value #VOCABULARY}.
 *
 * @author Martin Desruisseaux (Geomatys)
 * @version 3.20
 *
 * @since 3.20
 * @module
 */
public abstract class NetcdfMetadata implements WarningProducer {
    /**
     * The {@value} attribute name for a short description of the dataset
     * (<em>Highly Recommended</em>). If no {@value} attribute is provided,
     * then {@code NetcdfMetadata} will look for "full_name" and "name".
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getCitation() citation} /
     * {@link Citation#getTitle() title}</li></ul>
     *
     * @see NetcdfFile#getTitle()
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#title_Attribute">UCAR reference</a>
     */
    public static final String TITLE = "title";

    /**
     * The {@value} attribute name for a paragraph describing the dataset
     * (<em>Highly Recommended</em>).
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getAbstract() abstract}</li></ul>
     *
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#summary_Attribute">UCAR reference</a>
     */
    public static final String SUMMARY = "summary";

    /**
     * The {@value} attribute name for an identifier (<em>Recommended</em>).
     * The combination of the {@value #NAMING_AUTHORITY} and the {@value}
     * should be a globally unique identifier for the dataset.
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getFileIdentifier() fileIdentifier}</li>
     * <li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getCitation() citation} /
     * {@link Citation#getIdentifiers() identifier} /
     * {@link Identifier#getCode() code}</li></ul>
     *
     * @see NetcdfMetadataReader#getFileIdentifier()
     * @see NetcdfFile#getId()
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#id_Attribute">UCAR reference</a>
     */
    public static final String IDENTIFIER = "id";

    /**
     * The {@value} attribute name for the identifier authority (<em>Recommended</em>).
     * The combination of the {@value} and the {@value #IDENTIFIER} should be a globally
     * unique identifier for the dataset.
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getFileIdentifier() fileIdentifier}</li>
     * <li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getCitation() citation} /
     * {@link Citation#getIdentifiers() identifier} /
     * {@link Identifier#getAuthority() authority}</li></ul>
     *
     * @see #IDENTIFIER
     * @see NetcdfMetadataReader#getFileIdentifier()
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#id_Attribute">UCAR reference</a>
     */
    public static final String NAMING_AUTHORITY = "naming_authority";

    /**
     * The {@value} attribute name for a long descriptive name for the variable taken from a controlled
     * vocabulary of variable names. This is actually a {@linkplain VariableSimpleIF variable} attribute,
     * but sometime appears also in {@linkplain NetcdfFile#findGlobalAttribute(String) global attributes}.
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getDescriptiveKeywords() descriptiveKeywords} /
     * {@link Keywords#getKeywords() keyword} with {@link KeywordType#THEME}</li></ul>
     *
     * @see #STANDARD_NAME_VOCABULARY
     * @see #KEYWORDS
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#standard_name_Attribute">UCAR reference</a>
     */
    public static final String STANDARD_NAME = CF.STANDARD_NAME;

    /**
     * The {@value} attribute name for indicating which controlled list of variable names has been
     * used in the {@value #STANDARD_NAME} attribute.
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getDescriptiveKeywords() descriptiveKeywords} /
     * {@link Keywords#getThesaurusName() thesaurusName} /
     * {@link Citation#getTitle() title}</li></ul>
     *
     * @see #STANDARD_NAME
     * @see #VOCABULARY
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#standard_name_vocabulary_Attribute">UCAR reference</a>
     */
    public static final String STANDARD_NAME_VOCABULARY = "standard_name_vocabulary";

    /**
     * The {@value} attribute name for a comma separated list of key words and phrases
     * (<em>Highly Recommended</em>).
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getDescriptiveKeywords() descriptiveKeywords} /
     * {@link Keywords#getKeywords() keyword} with {@link KeywordType#THEME}</li></ul>
     *
     * @see #VOCABULARY
     * @see #STANDARD_NAME
     * @see NetcdfMetadataReader#getKeywordSeparator(Group)
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#keywords_Attribute">UCAR reference</a>
     */
    public static final String KEYWORDS = "keywords";

    /**
     * The {@value} attribute name for the guideline for the words/phrases in the
     * {@value #KEYWORDS} attribute (<em>Recommended</em>).
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getDescriptiveKeywords() descriptiveKeywords} /
     * {@link Keywords#getThesaurusName() thesaurusName} /
     * {@link Citation#getTitle() title}</li></ul>
     *
     * @see #KEYWORDS
     * @see #STANDARD_NAME_VOCABULARY
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#keywords_vocabulary_Attribute">UCAR reference</a>
     */
    public static final String VOCABULARY = "keywords_vocabulary";

    /**
     * The {@value} attribute name for a high-level geographic data thematic classification.
     * Typical values are {@code "farming"}, {@code "biota"}, {@code "boundaries"},
     * {@code "climatology meteorology atmosphere"}, {@code "economy"}, {@code "elevation"},
     * {@code "environment"}, {@code "geoscientific information"}, {@code "health"},
     * {@code "imagery base maps earth cover"}, {@code "intelligence military"},
     * {@code "inland waters"}, {@code "location"}, {@code "oceans"}, {@code "planning cadastre"},
     * {@code "society"}, {@code "structure"}, {@code "transportation"} and
     * {@code "utilitiesCommunication"}.
     *
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getTopicCategories() topicCategory}</li></ul>
     *
     * @see TopicCategory
     */
    public static final String TOPIC_CATEGORY = "topic_category";

    /**
     * The {@value} attribute name for the THREDDS data type appropriate for this dataset
     * (<em>Recommended</em>). Examples: {@code "Vector"}, {@code "TextTable"}, {@code "Grid"},
     * {@code "Image"}, {@code "Video"}, {@code "Tin"}, {@code "StereoModel"}, {@code "Station"},
     * {@code "Swath"} or {@code "Trajectory"}.
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getSpatialRepresentationTypes() spatialRepresentationType}</li></ul>
     *
     * @see SpatialRepresentationType
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#cdm_data_type_Attribute">UCAR reference</a>
     */
    public static final String DATA_TYPE = "cdm_data_type";

    /**
     * The {@value} attribute name for providing an audit trail for modifications to the
     * original data (<em>Recommended</em>).
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getDataQualityInfo() dataQualityInfo} /
     * {@link DataQuality#getLineage() lineage} /
     * {@link Lineage#getStatement() statement}</li></ul>
     *
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#history_Attribute">UCAR reference</a>
     */
    public static final String HISTORY = "history";

    /**
     * The {@value} attribute name for miscellaneous information about the data
     * (<em>Recommended</em>).
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getSupplementalInformation() supplementalInformation}</li></ul>
     *
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#comment_Attribute">UCAR reference</a>
     */
    public static final String COMMENT = "comment";

    /**
     * The {@value} attribute name for the date on which the metadata was created
     * (<em>Suggested</em>). This is actually defined in the "{@code NCISOMetadata}"
     * subgroup.
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getDateStamp() dateStamp}</li></ul>
     */
    public static final String METADATA_CREATION = "metadata_creation";

    /**
     * The {@value} attribute name for the date on which the data was created
     * (<em>Recommended</em>).
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getCitation() citation} /
     * {@link Citation#getDates() date} /
     * {@link CitationDate#getDate() date} with {@link DateType#CREATION}</li></ul>
     *
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#date_created_Attribute">UCAR reference</a>
     */
    public static final String DATE_CREATED = "date_created";

    /**
     * The {@value} attribute name for the date on which this data was last modified
     * (<em>Suggested</em>).
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getCitation() citation} /
     * {@link Citation#getDates() date} /
     * {@link CitationDate#getDate() date} with {@link DateType#REVISION}</li></ul>
     *
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#date_modified_Attribute">UCAR reference</a>
     */
    public static final String DATE_MODIFIED = "date_modified";

    /**
     * The {@value} attribute name for a date on which this data was formally issued
     * (<em>Suggested</em>).
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getCitation() citation} /
     * {@link Citation#getDates() date} /
     * {@link CitationDate#getDate() date} with {@link DateType#PUBLICATION}</li></ul>
     *
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#date_issued_Attribute">UCAR reference</a>
     */
    public static final String DATE_ISSUED = "date_issued";

    /**
     * Holds the attribute names describing a responsible party.
     * Values are:
     * <p>
     * <table border="1" cellspacing="0"><tr bgcolor="lightblue">
     *   <th>Attribute</th>
     *   <th>{@link NetcdfMetadata#CREATOR}</th>
     *   <th>{@link NetcdfMetadata#CONTRIBUTOR}</th>
     *   <th>{@link NetcdfMetadata#PUBLISHER}</th>
     * </tr><tr>
     *   <td>{@link #NAME}</td>
     *   <td>{@code "creator_name"}</td>
     *   <td>{@code "contributor_name"}</td>
     *   <td>{@code "publisher_name"}</td>
     * </tr><tr>
     *   <td>{@link #INSTITUTION}</td>
     *   <td>{@code "institution"}</td>
     *   <td></td>
     *   <td></td>
     * </tr><tr>
     *   <td>{@link #URL}</td>
     *   <td>{@code "creator_url"}</td>
     *   <td>{@code "contributor_url"}</td>
     *   <td>{@code "publisher_url"}</td>
     * </tr><tr>
     *   <td>{@link #EMAIL}</td>
     *   <td>{@code "creator_email"}</td>
     *   <td>{@code "contributor_email"}</td>
     *   <td>{@code "publisher_email"}</td>
     * </tr><tr>
     *   <td>{@link #ROLE}</td>
     *   <td></td>
     *   <td>{@code "contributor_role"}</td>
     *   <td></td>
     * </tr><tr>
     *   <td>{@link #DEFAULT_ROLE}</td>
     *   <td>{@link Role#ORIGINATOR}</td>
     *   <td></td>
     *   <td>{@link Role#PUBLISHER}</td>
     * </tr></table>
     *
     * {@note The member names in this class are upper-cases because they should be considered
     *        as constants. For example <code>NetcdfMetadata.CREATOR.EMAIL</code> maps exactly to the
     *        <code>"creator_email"</code> string and nothing else. A lower-case <code>email</code>
     *        member name could be misleading since it would suggest that the field contains the
     *        actual name value rather than the key by which the value is identified in a NetCDF file.}
     *
     * @author Martin Desruisseaux (Geomatys)
     * @version 3.20
     *
     * @since 3.20
     * @module
     */
    public static class Responsible {
        /**
         * The attribute name for the responsible's name. Possible values are
         * {@code "creator_name"}, {@code "contributor_name"} or {@code "publisher_name"}.
         * <p>
         * <b>Path:</b> <ul><li>{@link ResponsibleParty} /
         * {@link ResponsibleParty#getIndividualName() individualName}</li></ul>
         */
        public final String NAME;

        /**
         * The attribute name for the responsible's institution, or {@code null} if none.
         * Possible value is {@code "institution"}.
         * <p>
         * <b>Path:</b> <ul><li>{@link ResponsibleParty} /
         * {@link ResponsibleParty#getOrganisationName() organisationName}</li></ul>
         */
        public final String INSTITUTION;

        /**
         * The attribute name for the responsible's URL. Possible values are
         * {@code "creator_url"}, {@code "contributor_url"} or {@code "publisher_url"}.
         * <p>
         * <b>Path:</b> <ul><li>{@link ResponsibleParty} /
         * {@link ResponsibleParty#getContactInfo() contactInfo} /
         * {@link Contact#getOnlineResource() onlineResource} /
         * {@link OnlineResource#getLinkage() linkage}</li></ul>
         */
        public final String URL;

        /**
         * The attribute name for the responsible's email address. Possible values are
         * {@code "creator_email"}, {@code "contributor_email"} or {@code "publisher_email"}.
         * <p>
         * <b>Path:</b> <ul><li>{@link ResponsibleParty} /
         * {@link ResponsibleParty#getContactInfo() contactInfo} /
         * {@link Contact#getAddress() address} /
         * {@link Address#getElectronicMailAddresses() electronicMailAddress}</li></ul>
         */
        public final String EMAIL;

        /**
         * The attribute name for the responsible's role, or {@code null} if none.
         * Possible value is {@code "contributor_role"}.
         * <p>
         * <b>Path:</b> <ul><li>{@link ResponsibleParty} /
         * {@link ResponsibleParty#getRole()}</li></ul>
         *
         * @see Role
         */
        public final String ROLE;

        /**
         * The role to use as a fallback if no attribute value is associated to the {@link #ROLE} key.
         */
        public final Role DEFAULT_ROLE;

        /**
         * Creates a new set of attribute names. Any argument can be {@code null}
         * if not applicable.
         *
         * @param name        The attribute name for the responsible's name.
         * @param institution The attribute name for the responsible's institution.
         * @param url         The attribute name for the responsible's URL.
         * @param email       The attribute name for the responsible's email address.
         * @param role        The attribute name for the responsible's role.
         * @param defaultRole The role to use as a fallback if no attribute value is associated to the
         *                    {@code role} key.
         */
        public Responsible(final String name, final String institution, final String url, final String email,
                final String role, final Role defaultRole)
        {
            NAME         = name;
            INSTITUTION  = institution;
            URL          = url;
            EMAIL        = email;
            ROLE         = role;
            DEFAULT_ROLE = defaultRole;
        }
    }

    /**
     * The set of attribute names for the creator (<em>Recommended</em>).
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getCitation() citation} with {@link Role#ORIGINATOR}</li></ul>
     *
     * @see #CONTRIBUTOR
     * @see #PUBLISHER
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#creator_name_Attribute">UCAR reference</a>
     */
    public static final Responsible CREATOR = new Responsible("creator_name",
            "institution", "creator_url", "creator_email", null, Role.ORIGINATOR);

    /**
     * The set of attribute names for the contributor (<em>Suggested</em>).
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getCitation() citation}</li></ul>
     *
     * @see #CREATOR
     * @see #PUBLISHER
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#contributor_name_Attribute">UCAR reference</a>
     */
    public static final Responsible CONTRIBUTOR = new Responsible("contributor_name",
            null, "contributor_url", "contributor_email", "contributor_role", null);

    /**
     * The set of attribute names for the publisher (<em>Suggested</em>).
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getDistributionInfo() distributionInfo} /
     * {@link Distribution#getDistributors() distributors} /
     * {@link Distributor#getDistributorContact() distributorContact} with {@link Role#PUBLISHER}</li>
     * <li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getDescriptiveKeywords() descriptiveKeywords} /
     * {@link Keywords#getKeywords() keyword} with the {@code "dataCenter"} {@link KeywordType}</li></ul>
     *
     * @see #CREATOR
     * @see #CONTRIBUTOR
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#publisher_name_Attribute">UCAR reference</a>
     */
    public static final Responsible PUBLISHER = new Responsible("publisher_name",
            null, "publisher_url", "publisher_email", null, Role.PUBLISHER);

    /**
     * The {@value} attribute name for the scientific project that produced the data
     * (<em>Recommended</em>).
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getDescriptiveKeywords() descriptiveKeywords} /
     * {@link Keywords#getKeywords() keyword} with the {@code "project"} {@link KeywordType}</li></ul>
     *
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#project_Attribute">UCAR reference</a>
     */
    public static final String PROJECT = "project";

    /**
     * The {@value} attribute name for the summary of the intentions with which the resource(s)
     * was developed.
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getPurpose() purpose}</li></ul>
     */
    public static final String PURPOSE = "purpose";

    /**
     * The {@value} attribute name for bibliographical references.
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getCitation() citation} in its own instance</li></ul>
     */
    public static final String REFERENCES = "references";

    /**
     * The {@value} attribute name for a textual description of the processing (or quality control)
     * level of the data.
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getContentInfo() contentInfo} /
     * {@link ImageDescription#getProcessingLevelCode() processingLevelCode}</li></ul>
     *
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#processing_level_Attribute">UCAR reference</a>
     */
    public static final String PROCESSING_LEVEL = "processing_level";

    /**
     * The {@value} attribute name for a place to acknowledge various type of support for
     * the project that produced this data (<em>Recommended</em>).
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getCredits() credit}</li></ul>
     *
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#acknowledgement_Attribute">UCAR reference</a>
     */
    public static final String ACKNOWLEDGMENT = "acknowledgment";

    /**
     * The {@value} attribute name for a description of the restrictions to data access
     * and distribution (<em>Recommended</em>).
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getResourceConstraints() resourceConstraints} /
     * {@link LegalConstraints#getUseLimitations() useLimitation}</li></ul>
     *
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#license_Attribute">UCAR reference</a>
     */
    public static final String LICENSE = "license";

    /**
     * The {@value} attribute name for the access constraints applied to assure the protection of
     * privacy or intellectual property. Typical values are {@code "copyright"}, {@code "patent"},
     * {@code "patent pending"}, {@code "trademark"}, {@code "license"},
     * {@code "intellectual property rights"} or {@code "restricted"}.
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getResourceConstraints() resourceConstraints} /
     * {@link LegalConstraints#getAccessConstraints() accessConstraints}</li></ul>
     *
     * @see Restriction
     */
    public static final String ACCESS_CONSTRAINT = "acces_constraint";

    /**
     * The {@value} attribute name for an identifier of the geographic area.
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getExtents() extent} /
     * {@link Extent#getGeographicElements() geographicElement} /
     * {@link GeographicDescription#getGeographicIdentifier() geographicIdentifier}</li></ul>
     */
    public static final String GEOGRAPHIC_IDENTIFIER = "geographic_identifier";

    /**
     * Holds the attribute names describing a simple latitude, longitude, and vertical bounding box.
     * Values are:
     * <p>
     * <table border="1" cellspacing="0"><tr bgcolor="lightblue">
     *   <th>Attributes</th>
     *   <th>{@link NetcdfMetadata#LATITUDE}</th>
     *   <th>{@link NetcdfMetadata#LONGITUDE}</th>
     *   <th>{@link NetcdfMetadata#VERTICAL}</th>
     *   <th>{@link NetcdfMetadata#TIME}</th>
     * </tr><tr>
     *   <td>{@link #MINIMUM}</td>
     *   <td>{@code "geospatial_lat_min"}</td>
     *   <td>{@code "geospatial_lon_min"}</td>
     *   <td>{@code "geospatial_vertical_min"}</td>
     *   <td>{@code "time_coverage_start"}</td>
     * </tr><tr>
     *   <td>{@link #MAXIMUM}</td>
     *   <td>{@code "geospatial_lat_max"}</td>
     *   <td>{@code "geospatial_lon_max"}</td>
     *   <td>{@code "geospatial_vertical_max"}</td>
     *   <td>{@code "time_coverage_end"}</td>
     * </tr><tr>
     *   <td>{@link #SPAN}</td>
     *   <td></td>
     *   <td></td>
     *   <td></td>
     *   <td>{@code "time_coverage_duration"}</td>
     * </tr><tr>
     *   <td>{@link #RESOLUTION}</td>
     *   <td>{@code "geospatial_lat_resolution"}</td>
     *   <td>{@code "geospatial_lon_resolution"}</td>
     *   <td>{@code "geospatial_vertical_resolution"}</td>
     *   <td>{@code "time_coverage_resolution"}</td>
     * </tr><tr>
     *   <td>{@link #UNITS}</td>
     *   <td>{@code "geospatial_lat_units"}</td>
     *   <td>{@code "geospatial_lon_units"}</td>
     *   <td>{@code "geospatial_vertical_units"}</td>
     *   <td>{@code "time_coverage_units"}</td>
     * </tr><tr>
     *   <td>{@link #POSITIVE}</td>
     *   <td></td>
     *   <td></td>
     *   <td>{@code "geospatial_vertical_positive"}</td>
     *   <td></td>
     * </tr></table>
     *
     * {@note The member names in this class are upper-cases because they should be considered
     *        as constants. For example <code>NetcdfMetadata.LATITUDE.MINIMUM</code> maps exactly to
     *        the <code>"geospatial_lat_min"</code> string and nothing else. A lower-case
     *        <code>minimum</code> member name could be misleading since it would suggest that
     *        the field contains the actual name value rather than the key by which the value
     *        is identified in a NetCDF file.}
     *
     * @author Martin Desruisseaux (Geomatys)
     * @version 3.20
     *
     * @since 3.20
     * @module
     */
    public static class Dimension {
        /**
         * The attribute name for the minimal value of the bounding box (<em>Recommended</em>).
         * Possible values are {@code "geospatial_lat_min"}, {@code "geospatial_lon_min"},
         * {@code "geospatial_vertical_min"} and {@code "time_coverage_start"}.
         */
        public final String MINIMUM;

        /**
         * The attribute name for the maximal value of the bounding box (<em>Recommended</em>).
         * Possible values are {@code "geospatial_lat_max"}, {@code "geospatial_lon_max"},
         * {@code "geospatial_vertical_max"} and {@code "time_coverage_end"}.
         */
        public final String MAXIMUM;

        /**
         * The attribute name for the difference between the minimal and maximal values.
         * Possible value is {@code "time_coverage_duration"}.
         */
        public final String SPAN;

        /**
         * The attribute name for a further refinement of the geospatial bounding box
         * (<em>Suggested</em>). Possible values are {@code "geospatial_lat_resolution"},
         * {@code "geospatial_lon_resolution"}, {@code "geospatial_vertical_resolution"}
         * and {@code "time_coverage_resolution"}.
         */
        public final String RESOLUTION;

        /**
         * The attribute name for the bounding box units of measurement.
         * Possible values are {@code "geospatial_lat_units"}, {@code "geospatial_lon_units"},
         * {@code "geospatial_vertical_units"} and {@code "time_coverage_units"}.
         */
        public final String UNITS;

        /**
         * The attribute name for indicating which direction is positive (<em>Suggested</em>).
         * Possible value is {@code "geospatial_vertical_positive"}.
         */
        public final String POSITIVE;

        /**
         * Creates a new set of attribute names.
         *
         * @param min        The attribute name for the minimal value of the bounding box.
         * @param max        The attribute name for the maximal value of the bounding box.
         * @param span       The attribute name for the difference between the minimal and maximal values.
         * @param resolution The attribute name for a further refinement of the geospatial bounding box.
         * @param units      The attribute name for the bounding box units of measurement.
         * @param positive   The attribute name for indicating which direction is positive.
         */
        public Dimension(final String min, final String max, final String span, final String resolution,
                final String units, final String positive)
        {
            MINIMUM    = min;
            MAXIMUM    = max;
            SPAN       = span;
            RESOLUTION = resolution;
            UNITS      = units;
            POSITIVE   = positive;
        }
    }

    /**
     * The set of attribute names for the minimal and maximal latitudes of the bounding box,
     * resolution and units. Latitudes are assumed to be in decimal degrees north, unless a
     * units attribute is specified.
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getExtents() extent} /
     * {@link Extent#getGeographicElements() geographicElement} /
     * {@link GeographicBoundingBox#getSouthBoundLatitude() southBoundLatitude} or
     * {@link GeographicBoundingBox#getNorthBoundLatitude() northBoundLatitude}</li>
     * <li>{@link Metadata} /
     * {@link Metadata#getSpatialRepresentationInfo() spatialRepresentationInfo} /
     * {@link GridSpatialRepresentation#getAxisDimensionProperties() axisDimensionProperties} /
     * {@link org.opengis.metadata.spatial.Dimension#getResolution() resolution}</li></ul>
     *
     * @see #LONGITUDE
     * @see #VERTICAL
     * @see #TIME
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#geospatial_lat_min_Attribute">UCAR reference</a>
     */
    public static final Dimension LATITUDE = new Dimension("geospatial_lat_min",
            "geospatial_lat_max", null, "geospatial_lat_resolution", "geospatial_lat_units", null);

    /**
     * The set of attribute names for the minimal and maximal longitudes of the bounding box,
     * resolution and units. Longitudes are assumed to be in decimal degrees east, unless a
     * units attribute is specified.
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getExtents() extent} /
     * {@link Extent#getGeographicElements() geographicElement} /
     * {@link GeographicBoundingBox#getWestBoundLongitude() westBoundLongitude} or
     * {@link GeographicBoundingBox#getEastBoundLongitude() eastBoundLongitude}</li>
     * <li>{@link Metadata} /
     * {@link Metadata#getSpatialRepresentationInfo() spatialRepresentationInfo} /
     * {@link GridSpatialRepresentation#getAxisDimensionProperties() axisDimensionProperties} /
     * {@link org.opengis.metadata.spatial.Dimension#getResolution() resolution}</li></ul>
     *
     * @see #LATITUDE
     * @see #VERTICAL
     * @see #TIME
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#geospatial_lon_min_Attribute">UCAR reference</a>
     */
    public static final Dimension LONGITUDE = new Dimension("geospatial_lon_min",
            "geospatial_lon_max", null, "geospatial_lon_resolution", "geospatial_lon_units", null);

    /**
     * The set of attribute names for the minimal and maximal elevations of the bounding box,
     * resolution and units. Elevations are assumed to be in metres above the ground, unless a
     * units attribute is specified.
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getExtents() extent} /
     * {@link Extent#getVerticalElements() verticalElement} /
     * {@link VerticalExtent#getMinimumValue() minimumValue} or
     * {@link VerticalExtent#getMaximumValue() maximumValue}</li>
     * <li>{@link Metadata} /
     * {@link Metadata#getSpatialRepresentationInfo() spatialRepresentationInfo} /
     * {@link GridSpatialRepresentation#getAxisDimensionProperties() axisDimensionProperties} /
     * {@link org.opengis.metadata.spatial.Dimension#getResolution() resolution}</li></ul>
     *
     * @see #LATITUDE
     * @see #LONGITUDE
     * @see #TIME
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#geospatial_vertical_min_Attribute">UCAR reference</a>
     */
    public static final Dimension VERTICAL = new Dimension("geospatial_vertical_min",
            "geospatial_vertical_max", null, "geospatial_vertical_resolution",
            "geospatial_vertical_units", "geospatial_vertical_positive");

    /**
     * The set of attribute names for the start and end times of the bounding box, resolution and
     * units.
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getIdentificationInfo() identificationInfo} /
     * {@link DataIdentification#getExtents() extent} /
     * {@link Extent#getTemporalElements() temporalElement} /
     * {@link TemporalExtent#getExtent() extent}</li>
     * <li>{@link Metadata} /
     * {@link Metadata#getSpatialRepresentationInfo() spatialRepresentationInfo} /
     * {@link GridSpatialRepresentation#getAxisDimensionProperties() axisDimensionProperties} /
     * {@link org.opengis.metadata.spatial.Dimension#getResolution() resolution}</li></ul>
     *
     * @see #LATITUDE
     * @see #LONGITUDE
     * @see #VERTICAL
     * @see <a href="http://www.unidata.ucar.edu/software/netcdf-java/formats/DataDiscoveryAttConvention.html#time_coverage_start_Attribute">UCAR reference</a>
     */
    public static final Dimension TIME = new Dimension("time_coverage_start", "time_coverage_end",
            "time_coverage_duration", "time_coverage_resolution", "time_coverage_units", null);

    /**
     * The {@value} attribute name for the designation associated with a range element.
     * This attribute can be associated to {@linkplain VariableSimpleIF variables}. If
     * specified, they shall be one flag name for each {@linkplain #FLAG_MASKS flag mask},
     * {@linkplain #FLAG_VALUES flag value} and {@linkplain #FLAG_MEANINGS flag meaning}.
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getContentInfo() contentInfo} /
     * {@link CoverageDescription#getRangeElementDescriptions() rangeElementDescription} /
     * {@link RangeElementDescription#getName() name}</li></ul>
     */
    public static final String FLAG_NAMES = "flag_names";

    /**
     * The {@value} attribute name for bitmask to apply on sample values before to compare
     * them to the {@linkplain #FLAG_VALUES flag values}.
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getContentInfo() contentInfo} /
     * {@link CoverageDescription#getRangeElementDescriptions() rangeElementDescription} /
     * {@link RangeElementDescription#getRangeElements() rangeElement}</li></ul>
     */
    public static final String FLAG_MASKS = "flag_masks";

    /**
     * The {@value} attribute name for sample values to be flagged. The {@linkplain #FLAG_MASKS
     * flag masks}, flag values and {@linkplain #FLAG_MEANINGS flag meaning} attributes, used
     * together, describe a blend of independent boolean conditions and enumerated status codes.
     * A flagged condition is identified by a bitwise AND of the variable value and each flag masks
     * value; a result that matches the flag values value indicates a true condition.
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getContentInfo() contentInfo} /
     * {@link CoverageDescription#getRangeElementDescriptions() rangeElementDescription} /
     * {@link RangeElementDescription#getRangeElements() rangeElement}</li></ul>
     */
    public static final String FLAG_VALUES = "flag_values";

    /**
     * The {@value} attribute name for the meaning of {@linkplain #FLAG_VALUES flag values}.
     * Each flag values and flag masks must coincide with a flag meanings.
     * <p>
     * <b>Path:</b> <ul><li>{@link Metadata} /
     * {@link Metadata#getContentInfo() contentInfo} /
     * {@link CoverageDescription#getRangeElementDescriptions() rangeElementDescription} /
     * {@link RangeElementDescription#getDefinition() definition}</li></ul>
     */
    public static final String FLAG_MEANINGS = "flag_meanings";

    /**
     * Where to send the warnings, or {@code null} if none.
     */
    private final WarningProducer owner;

    /**
     * Creates a new metadata reader or writer for the given source or destination.
     *
     * @param owner Typically the {@link org.geotoolkit.image.io.SpatialImageReader} or
     *              {@link org.geotoolkit.image.io.SpatialImageWriter} object using this
     *              transcoder, or {@code null}.
     */
    protected NetcdfMetadata(final WarningProducer owner) {
        this.owner = owner;
    }

    /**
     * Invoked when a warning occurred. The default implementation delegates to the object
     * given at construction time if any, or logs to the {@link #LOGGER} otherwise.
     */
    @Override
    public boolean warningOccurred(final LogRecord record) {
        if (owner != null) {
            return owner.warningOccurred(record);
        }
        LOGGER.log(record);
        return false;
    }

    /**
     * Returns the locale to use for formatting {@linkplain #warningOccurred(LogRecord) warnings}.
     * The default implementation delegates to the object given at construction time, or returns
     * {@code null} if none.
     * <p>
     * Note that overriding this method does not guaranteed that warnings will be produced with
     * the new locale. The actually used locale depends on which class produced the warning
     * message.
     */
    @Override
    public Locale getLocale() {
        return (owner != null) ? owner.getLocale() : null;
    }
}