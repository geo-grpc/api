//
// Created by David Raleigh on 1/18/21.
//

#include "helper.h"

#include <stdlib.h>
#include <string.h>
#include <curl/curl.h>
#include <sys/time.h>
#include <jansson.h>
#include <pthread.h>

#define RENEW_TIME_BUFFER 100

// independent lock
static pthread_mutex_t jwt_lock;

size_t WriteMemoryCallback(void *contents, size_t size, size_t nmemb, void *userp)
{
    size_t realsize = size * nmemb;
    struct MemoryStruct *mem = (struct MemoryStruct *)userp;

    mem->memory = (char *)realloc(mem->memory, mem->size + realsize + 1);
    if(mem->memory == NULL) {
        /* out of memory! */
        printf("not enough memory (realloc returned NULL)\n");
        return 0;
    }

    memcpy(&(mem->memory[mem->size]), contents, realsize);
    mem->size += realsize;
    mem->memory[mem->size] = 0;

    return realsize;
}

char* get_auth_bearer(char* post_data, char* url_path, int isHttp2, int isJson) {
    // TODO THERE is NO ERROR CHECKING HERE!!!
    // https://github.com/salrashid123/jwt-samples/blob/master/gcs_auth.cc
    // https://sites.google.com/site/oauthgoog/Home/google-oauth2-assertion-flow
    struct MemoryStruct chunk;

    chunk.memory = (char *)malloc(1);  /* will be grown as needed by the realloc above */
    chunk.size = 0;    /* no data at this point */
    int res = CURLE_OK;
    CURL* curl_handle = curl_easy_init();
    if (curl_handle) {
        res = curl_easy_setopt(curl_handle, CURLOPT_URL, url_path);
        res = curl_easy_setopt(curl_handle, CURLOPT_WRITEFUNCTION, &WriteMemoryCallback);
        /* we pass our 'chunk' struct to the callback function */
        res = curl_easy_setopt(curl_handle, CURLOPT_WRITEDATA, (void *)&chunk);
        //curl_easy_setopt(curl, CURLOPT_VERBOSE, 1L);

        res = curl_easy_setopt(curl_handle, CURLOPT_POST, 1);

        if (isJson) {
            struct curl_slist *headers=NULL;
            headers = curl_slist_append(headers, "Accept: application/json");
            headers = curl_slist_append(headers, "Content-Type: application/json");
            headers = curl_slist_append(headers, "charset: utf-8");
            res = curl_easy_setopt(curl_handle, CURLOPT_HTTPHEADER, headers);
        }

        res = curl_easy_setopt(curl_handle, CURLOPT_POSTFIELDS, post_data);

        if (isHttp2) {
            res = curl_easy_setopt(curl_handle, CURLOPT_HTTP_VERSION, CURL_HTTP_VERSION_2_PRIOR_KNOWLEDGE);
        }
        /* we use a self-signed test server, skip verification during debugging */

        // TRUE by default as of cURL
        res = curl_easy_setopt(curl_handle, CURLOPT_SSL_VERIFYPEER, 1L);
        res = curl_easy_setopt(curl_handle, CURLOPT_SSL_VERIFYHOST, 2L);

        res = curl_easy_perform(curl_handle);

        /* check for errors */
        if (res != CURLE_OK) {
            fprintf(stderr, "curl_easy_perform() failed: %d\n", res);
            return NULL;
        } else {
            fprintf(stdout, "CURL get_auth_bear_gcs %lu bytes retrieved\n", (long)chunk.size);
        }
        curl_easy_cleanup(curl_handle);
    }


    char* output = strdup(chunk.memory);
    free(chunk.memory);

    return output;
}

char* get_nsl_access_token() {
    static char nsl_token[2056];
    static time_t nsl_expiration = 0;

    if (nsl_expiration > time(NULL)) {
        return nsl_token;
    }

    pthread_mutex_lock(&jwt_lock);
    if (nsl_expiration < time(NULL)) {
        json_t *json_obj, *access_token_obj, *expires_in_obj;
        json_error_t json_error;

        char* nsl_id = getenv("NSL_ID");
        char* secret = getenv("NSL_SECRET");
        char* api_aud = getenv("API_AUDIENCE");

        char* post_body = NULL;

        json_t *root = json_object();
        json_object_set_new( root, "client_id", json_string( nsl_id ) );
        json_object_set_new( root, "client_secret", json_string(secret) );
        json_object_set_new( root, "audience", json_string( api_aud ));
        json_object_set_new( root, "grant_type", json_string( "client_credentials" ) );
        post_body = json_dumps(root, 0);

        puts(post_body);
        json_decref(root);

        char *data = get_auth_bearer(post_body, "https://api.nearspacelabs.net/oauth/token", 0, 1);
        free(post_body);
        json_obj = json_loads(data, 0, &json_error);
        free(data);

        access_token_obj = json_object_get(json_obj, "access_token");
        const char *access_token = json_string_value(access_token_obj);
        snprintf(nsl_token, 2056, "%s", access_token);

        expires_in_obj = json_object_get(json_obj, "expires_in");
        long long int expires_in = json_integer_value(expires_in_obj);
        nsl_expiration = time(NULL) + expires_in - RENEW_TIME_BUFFER;

        json_delete(json_obj);
    }
    pthread_mutex_unlock(&jwt_lock);

    return nsl_token;
}
