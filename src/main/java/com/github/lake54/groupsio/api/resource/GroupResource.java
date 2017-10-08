package com.github.lake54.groupsio.api.resource;

import com.fasterxml.jackson.databind.JavaType;
import com.github.lake54.groupsio.api.GroupsIOApiClient;
import com.github.lake54.groupsio.api.domain.Error;
import com.github.lake54.groupsio.api.domain.Group;
import com.github.lake54.groupsio.api.domain.Page;
import com.github.lake54.groupsio.api.domain.Permissions;
import com.github.lake54.groupsio.api.exception.GroupsIOApiException;
import com.github.lake54.groupsio.api.jackson.TypeUtils;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.github.lake54.groupsio.api.domain.Error.Type.INADEQUATE_PERMISSIONS;

public class GroupResource extends BaseResource
{
    private static final JavaType GROUP_PAGE_TYPE = TypeUtils.generateType(factory -> {
        return factory.constructParametricType(Page.class, Group.class);
    });

    public GroupResource(final GroupsIOApiClient apiClient, final String baseUrl)
    {
        super(apiClient, baseUrl);
    }
    
    /**
     * Gets a user's {@link Permissions} for the specified group ID
     * 
     * @return the user's {@link Permissions} for the specified group ID
     * @throws URISyntaxException
     * @throws IOException
     * @throws GroupsIOApiException
     */
    public Permissions getPermissions(final Integer groupId) throws URISyntaxException, IOException, GroupsIOApiException
    {
        final URIBuilder uri = new URIBuilder().setPath(baseUrl + "getperms");
        uri.setParameter("group_id", groupId.toString());
        final HttpRequestBase request = new HttpGet();
        request.setURI(uri.build());
        
        return callApi(request, Permissions.class);
    }
    
    /**
     * Gets a {@link Group} for the specified group ID
     * 
     * @return the {@link Group} for the specified group ID
     * @throws URISyntaxException
     * @throws IOException
     * @throws GroupsIOApiException
     */
    public Group getGroup(final Integer groupId) throws URISyntaxException, IOException, GroupsIOApiException
    {
        if (apiClient.group().getPermissions(groupId).manageGroupSettings())
        {
            final URIBuilder uri = new URIBuilder().setPath(baseUrl + "getgroup");
            uri.setParameter("group_id", groupId.toString());
            final HttpRequestBase request = new HttpGet();
            request.setURI(uri.build());
            
            return callApi(request, Group.class);
        }
        else
        {
            final Error error = Error.create(INADEQUATE_PERMISSIONS);
            throw new GroupsIOApiException(error);
        }
    }
    
    /**
     * Gets a list of groups for a given group ID
     * 
     * @param groupId
     *            to fetch subgroups for
     * @return {@link List}<{@link Group}> belonging to a parent group.
     * @throws URISyntaxException
     * @throws IOException
     * @throws GroupsIOApiException
     */
    public List<Group> getSubgroups(final Integer groupId) throws URISyntaxException, IOException, GroupsIOApiException
    {
        final URIBuilder uri = new URIBuilder().setPath(baseUrl + "getsubgroups");
        uri.setParameter("group_id", groupId.toString());
        uri.setParameter("limit", MAX_RESULTS);
        final HttpRequestBase request = new HttpGet();
        request.setURI(uri.build());
        
        Page<Group> page = callApi(request, GROUP_PAGE_TYPE);
        final List<Group> subgroups = new ArrayList<>();
        subgroups.addAll(page.data());
        
        while (page.hasMore())
        {
            uri.setParameter("page_token", "" + page.nextPageToken());
            request.setURI(uri.build());
            page = callApi(request, GROUP_PAGE_TYPE);
            subgroups.addAll(page.data());
        }
        
        return subgroups;
    }
    
    public void createSubGroup(final Integer groupId, final String name, final String description, final String privacy)
    {
        throw new UnsupportedOperationException("Not implemented in API");
    }
    
    /**
     * Update a group given a blank {@link Group} object with only the updated
     * fields set.
     * Example:
     * 
     * <pre>
     * final Group groupToUpdate = new Group();
     * groupToUpdate.setWebsite("https://github.com/lake54/groupsio-api-java");
     * final Group updatedGroup = client.group().updateGroup(groupToUpdate);
     * </pre>
     * 
     * @param group
     *            - with only the updated fields set
     * @return the full {@link Group} after a successful update
     * @throws URISyntaxException
     * @throws IOException
     * @throws GroupsIOApiException
     */
    public Group updateGroup(final Group group) throws URISyntaxException, IOException, GroupsIOApiException
    {
        if (apiClient.group().getPermissions(group.id()).manageGroupSettings())
        {
            final URIBuilder uri = new URIBuilder().setPath(baseUrl + "updategroup");
            final HttpPost request = new HttpPost();
            final Map<String, Object> map = OM.convertValue(group, Map.class);
            final List<BasicNameValuePair> postParameters = new ArrayList<>();
            for (final Entry<String, Object> entry : map.entrySet())
            {
                postParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }
            request.setEntity(new UrlEncodedFormEntity(postParameters));
            
            request.setURI(uri.build());
            
            return callApi(request, Group.class);
        }
        else
        {
            final Error error = Error.create(INADEQUATE_PERMISSIONS);
            throw new GroupsIOApiException(error);
        }
    }
    
    public void deleteGroup(final Integer groupId)
    {
        throw new UnsupportedOperationException("Not implemented in API");
    }
    
}
