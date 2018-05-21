//server
#define MAXBUF 1024
#define MAXVALUE 20
#include<stdio.h>
#include<string.h>    //strlen
#include<stdlib.h>    //strlen
#include<sys/socket.h>
#include<arpa/inet.h> //inet_addr
#include<unistd.h>    //write
#include<pthread.h> //for threading , link with lpthread
 
//the thread function
void *connection_handler(void *);
struct configFile{
   char *key;
   char *value;
};

struct userInfoFile{
    char *username;
    char *password;
    char *friends[MAXVALUE];
    char *client_ip_address;
    int client_port;
    int online_status;
};

struct friendsOnline{
    char * username;
    char *ip_address;
    int port_number;
};

struct userInfoFile user_info_data[MAXVALUE];
int user_info_length;

struct friendsOnline friends_online[MAXVALUE];
int friends_online_length;

 
int main(int argc , char *argv[])
{
    struct configFile config_data[MAXVALUE];
    
    int port;
    int config_length = 0;
    user_info_length = 0;

    //reading configuration file for server
    FILE * fp ;
    //FILE * fp2;
    //char * line = NULL;
    ssize_t read;
    size_t len = 0;
    char *token;
    char **config;
    const char *delim1 = ":";
    const char *delim2 = "|";
    fp = fopen("/home/seed/Desktop/CPD_Project_2/server/configuration_file.cfg", "r");



    if (fp != NULL)
        { 
            char line[MAXBUF];
            int i = 0;
            int j = 0;
            while(fgets(line, sizeof(line), fp) != NULL)
            {
                j=0;         
                token = strtok(line, delim1);

                //config_data = realloc(config_data, i+1 * sizeof *config_data);
                while(token != NULL)
                {
                    
                    if(j == 0)
                    {
            config_data[i].key = (char *)malloc(sizeof(char) * (strlen(token)+1));
            strcpy(config_data[i].key, token);
                        //config_data[i].key = token;
                        //printf("%s\n", config_data[i].key);
                    }
                    else
                    {
            config_data[i].value = (char *)malloc(sizeof(char) * (strlen(token)+1));
            strcpy(config_data[i].value, token);
                        //config_data[i].value = token;
                        //printf("%s\n", config_data[i].value);
                    }

                    token = strtok(NULL, delim1);
                    j++;
                }
                i++;
            }
        config_length = i;
            }
    fclose(fp);

    fp = fopen("/home/seed/Desktop/CPD_Project_2/server/user_info_file.cfg", "r");
    if (fp != NULL)
        {                 
            char line[MAXBUF];
            int i = 0;
            int j = 0;

            while(fgets(line, sizeof(line), fp) != NULL)
            {
                j = 0;       
                token = strtok(line, delim2);
                while(token != NULL)
                {
                    
                    if(j == 0)
                    {
            user_info_data[i].username = (char *)malloc(sizeof(char) * (strlen(token)+1));
                        strcpy(user_info_data[i].username, token);
                        //printf("%s\n", user_info_data[i].username);

                    }
                    else if(j == 1)
                    {
            user_info_data[i].password = (char *)malloc(sizeof(char) * (strlen(token)+1));
                        strcpy(user_info_data[i].password, token);
                        //user_info_data[i].password = token;
                        //printf("%s\n", user_info_data[i].password);
                    }
                    else
                    {

                        const char *temp_delim = ";";
                        char * temp_token;
                        int k = 0;
                        temp_token = strtok(token, temp_delim);

                        while(temp_token != NULL)
                        {                   
                user_info_data[i].friends[k] = (char *)malloc(sizeof(char) * (strlen(temp_token)+1));
                            strcpy(user_info_data[i].friends[k],temp_token);         
                            //user_info_data[i].friends[k] = temp_token;
                            //printf("%s\n", user_info_data[i].friends[k]);
                            temp_token = strtok(NULL, temp_delim);
                            k++;
                        }
                    }
                    
                    token = strtok(NULL, delim2);
                    j++;
                }
                user_info_data[i].online_status = 0;
                i++;
            }
        user_info_length = i;
        }
    fclose(fp);

    int i;
    
    

    for(i=0 ; i<config_length; i++)
    {    
        if(strcmp(config_data[i].key,"port") == 0)
        {
     port = atoi(config_data[i].value);
        }
    }

    
 
    while(port == 0)
    {
    printf("Created port : %d\n", port);
        printf("Please enter an unused port number for user :");
    scanf("%d", &port);
    }

    
    
    int socket_desc , client_sock , c;
    struct sockaddr_in server , client;
     
    //Create socket
    socket_desc = socket(AF_INET , SOCK_STREAM , 0);
    if (socket_desc == -1)
    {
        printf("Could not create socket");
    }
    puts("Socket created");
     
    //Prepare the sockaddr_in structure
    server.sin_family = AF_INET;
    server.sin_addr.s_addr = INADDR_ANY;
    server.sin_port = htons( port );
     
    //Bind
    if( bind(socket_desc,(struct sockaddr *)&server , sizeof(server)) < 0)
    {
        //print the error message
        perror("bind failed. Error");
        return 1;
    }
    puts("binding done");
     
    //Listen
    listen(socket_desc , 3);

    

    printf("Domain Name : Localhost\n");
    printf("Port Number : %d\n", port);

    
    for(i = 0; i< user_info_length; i++)
    {
    
    printf("%s\n", user_info_data[i].username);
    
    printf("%s\n", user_info_data[i].password);
    
    
    int j;
    for (j = 0; user_info_data[i].friends[j] != NULL; j++)
    {
        printf("%s\n", user_info_data[i].friends[j]);
    }
    
    }


     
    //Accept and incoming connection
    puts("Waiting for incoming connections...");
    c = sizeof(struct sockaddr_in);
        
    pthread_t thread_id;
    
    while( (client_sock = accept(socket_desc, (struct sockaddr *)&client, (socklen_t*)&c)) )
    {
        puts("Connection accepted");
         
        if( pthread_create( &thread_id , NULL ,  connection_handler , (void*) &client_sock) > 0)
        {
            perror("could not create thread");
            return 1;
        }
         
        //Now join the thread , so that we dont terminate before the thread
        //pthread_join( thread_id , NULL);
        puts("Handler assigned");
    }
     
    if (client_sock < 0)
    {
        perror("accept failed");
        return 1;
    }

    close(socket_desc);
     
    return 0;
}
 
/*
 * This will handle connection for each client
 * */
void *connection_handler(void *socket_desc)
{
    //Get the socket descriptor
    int sock = *(int*)socket_desc;
    int read_size;
    char *message , client_message[2000];
    int cmd_size;
    char cmd[50];
    char response[1000];
    int res_size;
    char username[1000];
    char password[1000];
    char loc_ip[50];
    char loc_port[20];
     
    //Send some messages to the client
    message = "What would you like to do?\nr\t-\tregister with the server\nl\t-\tlog into the server\nexit\t-\texit the client program\n";
    write(sock , message , strlen(message));

    if((cmd_size = recv(sock , cmd , 50 , 0)) > 0)
    {

    printf("cmd size : %d", cmd_size);
    cmd[cmd_size-1] = '\0';
    printf("Client cmd : %s\n",cmd);    
    }
    else
    {
    perror("recv failed");    
    }    

    int success = 0;
    if (strcmp(cmd, "r")== 0 || strcmp(cmd, "l") == 0)
    {
        while(!(success == 1))
        {
            message = "Enter Username : \n";
            write(sock , message , strlen(message));
            if((res_size = recv(sock , username , 1000 , 0)) > 0)
            {
            username[res_size-1] = '\0'; 
            printf("Client username : %s\n", username);
            }
            else
            {
            perror("recv failed");    
            }    

            message = "Enter Password : \n";
            write(sock , message , strlen(message));
            if((res_size = recv(sock , password , 1000 , 0)) > 0)
            {
            password[res_size-1] = '\0';
            printf("Client password : %s\n",password);
            }
            else
            {
            perror("recv failed");    
            }

            if(strcmp(cmd, "r") == 0)
            {
                int i;
                int flag = 0;
                for (i = 0; i < user_info_length; i++)
                {
                    if(strcmp(username, user_info_data[i].username) == 0)
                    {
                        memset(username, 0, 1000);
                        memset(password, 0, 1000);
                        message = "500";
                        write(sock , message , strlen(message));                                            
                        flag = 1;
                        break;
                    }
                }
                if(flag == 0)
                {
                    user_info_data[i].username = (char *)malloc(sizeof(char) * (strlen(username)+1));
                    strcpy(user_info_data[i].username, username);
                    user_info_data[i].password = (char *)malloc(sizeof(char) * (strlen(password)+1));
                    strcpy(user_info_data[i].password, password);
                    user_info_length = user_info_length + 1;
                    message = "200";
                    write(sock , message , strlen(message));
                    success = 1;
                }
            }
            else if(strcmp(cmd, "l") == 0)
            {
                int i;
                int flag = 0;
                for (i = 0; i < user_info_length; i++)
                {
                    if(strcmp(username, user_info_data[i].username) == 0)
                    {
                        if(strcmp(password, user_info_data[i].password) == 0)
                        {
                            message = "200";
                            write(sock , message , strlen(message));                                            
                            flag = 1;
                            success = 1;
                            break;                                                
                        }                                          
                        
                    }
                }
                if(flag == 0)
                {
                    memset(username, 0, 1000);
                    memset(password, 0, 1000);
                    message = "500";
                    write(sock , message , strlen(message));                                        
                }            
            } 
        }                          
    }

    // if counter is here meaning logging in is successful
    if((res_size = recv(sock , loc_ip , 50 , 0)) > 0)
            {
            loc_ip[res_size-1] = '\0'; 
            printf("Client location ip : %s\n", loc_ip);
            }
            else
            {
            perror("recv failed");    
            }   

    if((res_size = recv(sock , loc_port , 20 , 0)) > 0)
            {
            loc_port[res_size-1] = '\0'; 
            printf("Client location port : %s\n", loc_port);
            }
            else
            {
            perror("recv failed");    
            }    


    //this has to be locked
    int i;
    for (i = 0; i < user_info_length; i++)
        {
            if(strcmp(username, user_info_data[i].username) == 0)
                {
                    user_info_data[i].client_ip_address = (char *)malloc(sizeof(char) * (strlen(loc_ip)+1));
                    strcpy(user_info_data[i].client_ip_address, loc_ip);
                    user_info_data[i].client_port = atoi(loc_port);
                    user_info_data[i].online_status = 1;
                }
        }


    printf("%s : %s", username, password) ;
    message = "Now type something and i shall repeat what you type \n";
    write(sock , message , strlen(message));
     
    //Receive a message from client
    while( (read_size = recv(sock , client_message , 2000 , 0)) > 0 )
    {
        //end of string marker
        client_message[read_size-1] = '\0';
        
        //Send the message back to client
        write(sock , client_message , strlen(client_message));
    printf("M: %s", client_message);

        
        //clear the message buffer
        memset(client_message, 0, 2000);
    }
     
    if(read_size == 0)
    {
        puts("Client disconnected");
        fflush(stdout);
    }
    else if(read_size == -1)
    {
        perror("recv failed###");
    }
         
    return 0;
}
 