/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.commonservices.generichandlers;


import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.InstanceProperties;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.repositoryconnector.OMRSRepositoryHelper;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.TypeErrorException;


/**
 * NoteLogBuilder is able to build the properties for a NoteLog entity.
 */
public class NoteLogBuilder extends ReferenceableBuilder
{
    private final String  name;
    private final String  description;
    private final boolean isPublic;

    /**
     * Constructor.
     *
     * @param qualifiedName unique name (qualifiedName) for the note log
     * @param name  name of the note log
     * @param description   description
     * @param isPublic      should this be visible to all or private to the caller
     * @param repositoryHelper helper methods
     * @param serviceName name of this OMAS
     * @param serverName name of local server
     */
    public NoteLogBuilder(String               qualifiedName,
                          String               name,
                          String               description,
                          boolean              isPublic,
                          OMRSRepositoryHelper repositoryHelper,
                          String               serviceName,
                          String               serverName)
    {
        super(qualifiedName,
              OpenMetadataAPIMapper.NOTE_LOG_TYPE_GUID,
              OpenMetadataAPIMapper.NOTE_LOG_TYPE_NAME,
              repositoryHelper,
              serviceName,
              serverName);

        this.description = description;
        this.name = name;
        this.isPublic = isPublic;
    }


    /**
     * Return the relationship properties in an InstanceProperties object.
     *
     * @param methodName name of the calling method
     * @return InstanceProperties object
     */
    public InstanceProperties getRelationshipInstanceProperties(String  methodName)
    {
        InstanceProperties properties = repositoryHelper.addBooleanPropertyToInstance(serviceName,
                                                                                      null,
                                                                                      OpenMetadataAPIMapper.IS_PUBLIC_PROPERTY_NAME,
                                                                                      isPublic,
                                                                                      methodName);

        setEffectivityDates(properties);

        return properties;
    }


    /**
     * Return the entity properties in an InstanceProperties object.
     *
     * @param methodName name of the calling method
     * @return InstanceProperties object
     * @throws InvalidParameterException there is a problem with the properties
     */
    @Override
    public InstanceProperties getInstanceProperties(String  methodName) throws InvalidParameterException
    {
        InstanceProperties properties = super.getInstanceProperties(methodName);

        properties = repositoryHelper.addStringPropertyToInstance(serviceName,
                                                                  properties,
                                                                  OpenMetadataAPIMapper.NAME_PROPERTY_NAME,
                                                                  name,
                                                                  methodName);

        properties = repositoryHelper.addStringPropertyToInstance(serviceName,
                                                                  properties,
                                                                  OpenMetadataAPIMapper.DESCRIPTION_PROPERTY_NAME,
                                                                  description,
                                                                  methodName);

        return properties;
    }
}
