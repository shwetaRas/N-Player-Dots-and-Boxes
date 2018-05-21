#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include <signal.h>
#include <netdb.h>

#define MAXBUF 1024
#define MAX_SIZE 50
#define MAXVALUE 20

void *handelStdin(void *arg);
void *handelServer(void *arg);
void *handleFriend(void *sock);
void allowConnections();
void *handleConnection(void *arg);
void closeLocalSockets();
void terminateFriendThreads();
void termination_handler(int sig_num);
void exitHandler();
void displayCommands();
int SendMessage(char *username, char*message);
int AlreadyFriend(char *username);
int HasSentInvite(char *username);               
int ReceivedInviteFrom(char *username);
void RemoveInvite(char *username);
void AddFriendInfo(char *username, char * ip_address, char *port);
void AddReceivedInvite(char *username);
void  removeSentInvite(char *username);
void RemoveFriendInfo(char *username);           
void RemoveConnectedFriend(char *username);
void PrintMessageFromFriend(int sock, char *message);
void AddConnectedThread(int sock, pthread_t pid);

struct configFile{
   char *key;
   char *value;
};

struct friendsInfo
{
    char *username;
    char *ip_address;
    int port;
};

struct connectedFriends
{
    int sock_desc;
    char *username;
};

struct connectedThreads
{
    int socket;
    pthread_t pid;
};

int logged_in;
int local_socket;
int server_socket;

char *client_username;
char *server_hostname;

struct friendsInfo friends_info[MAXVALUE];
int friends_info_length;

struct connectedFriends connected_friends[MAXVALUE];
int connected_friends_length;

struct connectedThreads connected_threads[MAXVALUE];
int connected_threads_length;

char *received_invites[MAXVALUE];
int received_invites_length;

char *sent_invites[MAXVALUE];
int sent_invites_length;

pthread_t server_thread;
pthread_t connection_thread;
pthread_t stdin_thread;

pthread_attr_t joined_thread_attr;
pthread_attr_t detached_thread_attr;

pthread_mutex_t friends_info_mutex = PTHREAD_MUTEX_INITIALIZER;;
pthread_mutex_t connected_friends_mutex = PTHREAD_MUTEX_INITIALIZER;;
pthread_mutex_t connected_threads_mutex = PTHREAD_MUTEX_INITIALIZER;;
pthread_mutex_t received_invites_mutex = PTHREAD_MUTEX_INITIALIZER;;
pthread_mutex_t sent_invites_mutex = PTHREAD_MUTEX_INITIALIZER;;

int main(int argc , char *argv[])
{
    if(argc != 2)
    {
        perror("\nInput format : ./messenger_client configuration_file");
        exit(EXIT_FAILURE);
    }

    signal(SIGINT, termination_handler);

    FILE * fp ;
    char *token;
    const char *delim1 = ":";
    struct configFile config_data[MAXVALUE];
    int config_length = 0;

    fp = fopen(argv[1], "r");
    if (fp == NULL)
    {
        perror("\nconfiguration_file does not exist.");
        exit(EXIT_FAILURE);
    }
    else
    {
    char line[MAXBUF];
    int i = 0;
    int j = 0;
    while(fgets(line, sizeof(line), fp) != NULL)
    {
        j=0;         
        token = strtok(line, delim1);

        while(token != NULL)
        {       

        if(token[strlen(token)-1] == '\n')
        token[strlen(token) - 1] =0;  

            if(j == 0)
            {
                config_data[i].key = (char *)malloc(sizeof(char) * (strlen(token)+1));
                strcpy(config_data[i].key, token);
            }
            else
            {
                config_data[i].value = (char *)malloc(sizeof(char) * (strlen(token)+1));
                strcpy(config_data[i].value, token);
            }

            token = strtok(NULL, delim1);
            j++;
        }
        i++;
    }
    config_length = i;        
    }    
    fclose(fp);


    char *server_host;
    char *server_port;
    int i;
    for (i=0;i<config_length;i++)
    {
        if(strcmp(config_data[i].key, "servhost") == 0)
        {
            server_host = (char *)malloc(sizeof(char) * (strlen(config_data[i].value)+1));
            strcpy(server_host, config_data[i].value);
        printf("server_host : %s", server_host);
        }
        if(strcmp(config_data[i].key, "servport") == 0)
        {
            server_port = (char *)malloc(sizeof(char) * (strlen(config_data[i].value)+1));
            strcpy(server_port, config_data[i].value);
        printf("server_port : %s", server_port);
        }

    }

    

    if(server_host == NULL)
    {
        perror("Server Host not defined\n");
        exit(EXIT_FAILURE);
    }

    if(server_port == NULL)
    {
        perror("Server port not valid\n");
        exit(EXIT_FAILURE);
    }

    struct addrinfo hints;
    struct addrinfo *info;

    memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_CANONNAME;

    int s = getaddrinfo(server_host, server_port, &hints, &info);
    if(s != 0)
    {
        printf("Failed to get server address information :  %s\n", gai_strerror(s));
        exit(EXIT_FAILURE);
    }


    //int sock_desc;
    //int server_sock;
    //struct sockaddr_in serv_addr;
    //char sbuff[MAX_SIZE],rbuff[MAX_SIZE];
    //pthread_t thread_id;


    if((server_socket = socket(info->ai_family, info->ai_socktype, info->ai_protocol)) < 0)
    {
        printf("Failed creating socket\n");
        exit(EXIT_FAILURE);
    }

    // bzero((char *) &serv_addr, sizeof (serv_addr));

    // serv_addr.sin_family = AF_INET;
    // serv_addr.sin_addr.s_addr = inet_addr("127.0.0.1");
    // serv_addr.sin_port = htons(5558);

    if (connect(server_socket, info->ai_addr, info->ai_addrlen) < 0) {
        printf("Failed to connect to server %s on port %s\n", server_host, server_port);
        exit(EXIT_FAILURE);
    }

    printf("You are connected to %s on port %s", server_host, server_port);

    logged_in = 0;
    server_hostname = (char *)malloc(sizeof(char) * (strlen(server_host)+1));
    strcpy(server_hostname, server_host);

    pthread_attr_init(&joined_thread_attr);
    pthread_attr_setdetachstate(&joined_thread_attr, PTHREAD_CREATE_JOINABLE);

    pthread_attr_init(&detached_thread_attr);
    pthread_attr_setdetachstate(&detached_thread_attr, PTHREAD_CREATE_DETACHED);

    if( pthread_create( &stdin_thread, &joined_thread_attr , handelStdin , NULL) > 0)
        {
            perror("Could not create Standard Input thread");
            exit(EXIT_FAILURE);
        }

    if( pthread_create( &server_thread , &joined_thread_attr , handelServer , NULL) > 0)
        {
            perror("Could not create Server Thread");
            exit(EXIT_FAILURE);
        }

    pthread_join(stdin_thread, NULL);
    pthread_join(server_thread, NULL);

    // while(fgets(sbuff, MAX_SIZE , stdin)!=NULL)
    // {
    //   //send(sock_desc,sbuff,strlen(sbuff),0);
    //   printf("%s", sbuff);
    //   write(sock_desc, sbuff, strlen(sbuff));   
    //   memset(sbuff, 0, strlen(sbuff));

    // }

    // close(sock_desc);
    return EXIT_SUCCESS;

}

void *handelStdin(void *arg)
{
    char buffer[256];
    char input_string[2000];

    while(1)
    {
        if(fgets(input_string, 2000, stdin)!=NULL)
        {
        char *command_arr[MAXVALUE];
        int command_len;

        char *token;
        const char *del = " ";
        int i = 0;

        token = strtok(input_string, del);
        while(token != NULL)
        {
            if(token[strlen(token)-1] == '\n')
                token[strlen(token) - 1] = 0;

            command_arr[i] = (char *)malloc(sizeof(char) * (strlen(token)+1));
            strcpy(command_arr[i], token); 
            token = strtok(NULL, del);
            i++;
        }

        
        for(i=0; command_arr[i] != NULL; i++)
        {            
        }
        command_len = i;

        if(logged_in == 0)
        {

            //r username password
            if(strcmp(command_arr[0], "r") == 0)
            {
                if(command_len < 3)
                {
                    printf("\nCommand format : r [username] [password]");
                    continue;
                }
                snprintf(buffer, sizeof(buffer), "r %s %s", command_arr[1], command_arr[2]);
                write(server_socket, buffer, sizeof(buffer));
                
            }
            //l username password
            else if(strcmp(command_arr[0], "l") == 0)
            {
                if(command_len < 3)
                {
                    printf("\nCommand format : l [username] [password]");
                    continue;
                }
                snprintf(buffer, sizeof(buffer), "l %s %s", command_arr[1], command_arr[2]);
                write(server_socket, buffer, sizeof(buffer));
            }   
            //help
            else if(strcmp(command_arr[0], "help") == 0)
            {
                displayCommands();
            }      
            //exit
            else if(strcmp(command_arr[0], "exit") == 0)
            {
                strncpy(buffer, "exit", sizeof(buffer));
                write(server_socket, buffer, sizeof(buffer));
                close(server_socket);
                exit(EXIT_SUCCESS);
            }
            else
            {
                printf("\nCommand Not recognised!");
            }

        }
        else
        {
            //m username message
            if(strcmp(command_arr[0], "m") == 0)
            {
                if(command_len < 3)
                {
                    printf("\nCommand format : m [username] [message]");
                    continue;
                }   
                if(strcmp(command_arr[1], client_username) == 0)
                {
                    printf("\nCannot message yourself");
                    continue;
                }
                if( SendMessage(command_arr[1], command_arr[2]) < 0)
                {
                    continue;
                }
            }  
            //i username message
            else if(strcmp(command_arr[0], "i") == 0)
            {
                if(command_len < 3)
                {
                    printf("\nCommand format : i [username] [message]");
                    continue;
                }

                if(strcmp(command_arr[1], client_username) == 0)
                {
                    printf("\nSending invitation to onself not allowed.");
                    continue;
                }

                pthread_mutex_lock(&friends_info_mutex);
                if(AlreadyFriend(command_arr[1]) < 0)
                {
                    printf("\nYou are already friends with %s", command_arr[1]);
                    continue;
                }
                pthread_mutex_unlock(&friends_info_mutex);  

                pthread_mutex_unlock(&sent_invites_mutex);
                if(HasSentInvite(command_arr[1]) < 0)
                {
                    printf("\nYou had already sent an invite to %s", command_arr[1]);
                    continue;

                }
                pthread_mutex_unlock(&sent_invites_mutex);

                pthread_mutex_unlock(&received_invites_mutex);
                if(ReceivedInviteFrom(command_arr[1]) < 0)
                {
                    printf("\nYou have a pending invite from %s", command_arr[1]);
                    continue;
                }
                pthread_mutex_unlock(&received_invites_mutex);

                snprintf(buffer, sizeof(buffer), "invite %s %s", command_arr[1], command_arr[2]);
                write(server_socket, buffer, sizeof(buffer));

                pthread_mutex_lock(&sent_invites_mutex);

                sent_invites[sent_invites_length] = (char *)malloc(sizeof(char) * (strlen(command_arr[1])+1));
                strcpy(sent_invites[sent_invites_length], command_arr[1]);
                sent_invites_length = sent_invites_length + 1;

                pthread_mutex_unlock(&sent_invites_mutex);
            }
            //ia username message
            else if(strcmp(command_arr[0], "ia") == 0)
            {
                if(command_len < 3)
                {
                    printf("\nCommand format : ia [username] [message]");
                    continue;
                }
                pthread_mutex_unlock(&received_invites_mutex);
                if(ReceivedInviteFrom(command_arr[1]) < 0)
                {
                    snprintf(buffer, sizeof(buffer), "invite_accept %s %s", command_arr[1], command_arr[2]);
                    write(server_socket, buffer, sizeof(buffer));
                    RemoveInvite(command_arr[1]);
                }
                else
                {
                    printf("\nYou have not received an invite from %s", command_arr[1]);
                }
                
                pthread_mutex_unlock(&received_invites_mutex);

            }
            //logout
            else if(strcmp(command_arr[0], "logout") == 0)
            {
                strncpy(buffer, "logout", sizeof(buffer));
                write(server_socket, buffer, sizeof(buffer));

                pthread_cancel(connection_thread);
                terminateFriendThreads();
                closeLocalSockets();

                friends_info_length = 0;
                connected_friends_length = 0;
                sent_invites_length = 0;
                received_invites_length = 0;

                printf("\nYou have logged out of the server");
                logged_in = 0;                
            }
            else
            {
                printf("\nCommand not recognized!");
            }

        }
    }
    }

    return;
}


void *handelServer(void *arg)
{
    char server_response[2000];
    int read_size;

    while(1)
    {
        if((read_size = recv(server_socket , server_response, 2000 , 0)) > 0)
        {
        char *command_arr[MAXVALUE];

        char *token;
        const char *del = " ";
        int i = 0;

        token = strtok(server_response, del);
        while(token != NULL)
        {
            if(token[strlen(token)-1] == '\n')
                token[strlen(token) - 1] =0;

            command_arr[i] = (char *)malloc(sizeof(char) * (strlen(token)+1));
            strcpy(command_arr[i], token); 
            token = strtok(NULL, del);
            i++;
        }

        //r username status_code
        if(strcmp(command_arr[0], "r") == 0)
        {
            if(strcmp(command_arr[2], "200") == 0)
            {
                printf("\nYou have successfully registered as %s. Please Login!", command_arr[1]);                
            }
            else
            {
                printf("\nEntered username %s is unavailable. Please enter another.", command_arr[1]);
            }
        }
        //l username status code
        else if (strcmp(command_arr[0], "l") == 0)
        {
            if(strcmp(command_arr[2], "200") == 0)
            {
                printf("\nYou have successfully logged in as %s", command_arr[1]);   
                logged_in = 1;    
                client_username = (char *)malloc(sizeof(char) * (strlen(command_arr[1])+1));
                strcpy(client_username, command_arr[1]);
                allowConnections();         
            }
            else
            {
                printf("\nIncorrect Credentials for user %s.Try Again.", command_arr[1]);
            }

        }
        //loc username ip_address port
        else if (strcmp(command_arr[0], "loc") == 0)
        {
            printf("Friend %s is online", command_arr[1]);

            pthread_mutex_lock(&friends_info_mutex);

            AddFriendInfo(command_arr[1], command_arr[2], command_arr[3]);

            pthread_mutex_lock(&friends_info_mutex);

        }
        //invite_from username message
        else if (strcmp(command_arr[0], "invite_from") == 0)
        {
            printf("You have received an invite from %s : %s\n", command_arr[1], command_arr[2]);
            pthread_mutex_lock(&received_invites_mutex);
            AddReceivedInvite(command_arr[1]);
            pthread_mutex_unlock(&received_invites_mutex);
        }
        //invite_accept username message
        else if (strcmp(command_arr[0], "invite_accept") == 0)
        {
            printf("%s has accpeted your invitation : %s", command_arr[1], command_arr[2]);
            pthread_mutex_lock(&sent_invites_mutex);
            removeSentInvite(command_arr[1]);
            pthread_mutex_lock(&sent_invites_mutex);
        }
        //invite_failed username
        else if (strcmp(command_arr[0], "invite_failed") == 0)
        {
            printf("Failed to send invite to %s. User does not exist.", command_arr[1]);
            pthread_mutex_lock(&sent_invites_mutex);
            removeSentInvite(command_arr[1]);
            pthread_mutex_lock(&sent_invites_mutex);

        }
        //shutdown
        else if (strcmp(command_arr[0], "shutdown") == 0)
        {
            printf("Server has shutdown.");
            exitHandler();
        }
        //logout username or terminate username
        else if (strcmp(command_arr[0], "logout") == 0 || strcmp(command_arr[0], "terminate") == 0 )
        {
            printf("%s has logged out.", command_arr[1]);

            pthread_mutex_lock(&friends_info_mutex);
            RemoveFriendInfo(command_arr[1]);
            pthread_mutex_unlock(&friends_info_mutex);

            pthread_mutex_lock(&connected_friends_mutex);
            RemoveConnectedFriend(command_arr[1]);
            pthread_mutex_unlock(&connected_friends_mutex);
        }
        else
        {
            printf("%s", server_response);
        }

    }
}

    return;
}


void *handleFriend(void *sock)
{
    int sock_fd = *(int*)sock;
    char response[256];
    int read_size;

    while(1)
    {
        if((read_size = recv(sock_fd , response, 256 , 0)) > 0)
        {
            char *command_arr[MAXVALUE];

            char *token;
            const char *del = " ";
            int i = 0;

            token = strtok(response, del);
            while(token != NULL)
            {
                if(token[strlen(token)-1] == '\n')
                    token[strlen(token) - 1] = 0;
                
                command_arr[i] = (char *)malloc(sizeof(char) * (strlen(token)+1));
                strcpy(command_arr[i], token); 
                token = strtok(NULL, del);
                i++;
            }

            pthread_mutex_lock(&connected_friends_mutex);

            //user username
            if (strcmp(command_arr[0], "user") == 0)
            {
                
                connected_friends[connected_friends_length].sock_desc = sock_fd;
                connected_friends[connected_friends_length].username = (char *)malloc(sizeof(char) * (strlen(command_arr[1])+1));
                strcpy(connected_friends[connected_friends_length].username, command_arr[1]);
                
            }
            else
            {
                PrintMessageFromFriend(sock_fd, response);
            }

            pthread_mutex_lock(&connected_friends_mutex);
        }

    }

    return;
}


void allowConnections()
{

    struct sockaddr_in client;
    socklen_t client_length;
    struct addrinfo hints;
    struct addrinfo *info;
    char buffer[256]; 
    char hostname[256];

    memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_CANONNAME;


    // char *c_ip = "127.0.0.1";
    // int c_port = 5100;
    
    //Create socket
    local_socket = socket(AF_INET , SOCK_STREAM , 0);
    if (local_socket == -1)
    {
        printf("\nCould not create socket");
        exit(EXIT_FAILURE);
    }
    

    //Prepare the sockaddr_in structure
    memset(&client, 0, sizeof(client));
    client.sin_family = AF_INET;
    client.sin_addr.s_addr = htonl(INADDR_ANY);
    client.sin_port = htons(0);

    client_length = sizeof(client);

    //Bind
    if( bind(local_socket,(struct sockaddr *)&client , client_length) < 0)
    {
    //print the error message
    perror("\nbind failed. Error");
    exit(EXIT_FAILURE);
    }
    
    //Listen
    if (listen(local_socket, 5) < 0) 
    {
        perror("\nFailed to set local socket as passive");
        exit(EXIT_FAILURE);
    }   


    if(gethostname(hostname, sizeof(hostname)) < 0)
    {
        printf("\nFalied to get server hostname.");
        exit(EXIT_FAILURE);
    }
    
    if(getsockname(local_socket, (struct sockaddr *)&client, &client_length ) < 0)
    {
        printf("\n Failed to get address to which client local socket is bound.");
        exit(EXIT_FAILURE);
    }

    if(getaddrinfo(hostname, NULL, &hints, &info) != 0)
    {
        printf("Failed to get client address information.");
        exit(EXIT_FAILURE);
    }


    //send connection info to the server


    snprintf(buffer, sizeof(buffer), "loc %s %d", info->ai_canonname, ntohs(client.sin_port));
    write(server_socket, buffer, sizeof(buffer));

    
    if(pthread_create(&connection_thread, &detached_thread_attr, handleConnection, NULL) != 0)
    {
        printf("Failed to create thread to accept connections\n");
        exit(EXIT_FAILURE);
    }


}


void *handleConnection(void *arg)
{
    int new_socket;

    struct sockaddr_in new_addr;

    pthread_t new_thread;

    int new_addr_len = sizeof(struct sockaddr_in);

    while(1)
    {
        if((new_socket = accept(local_socket, (struct sockaddr *)&new_addr, (socklen_t*)&new_addr_len) ) >= 0)
        {
            if(pthread_create(&new_thread, &detached_thread_attr, handleFriend, (void*)&new_socket) != 0)
            {
                continue;
            }
            pthread_mutex_lock(&connected_threads_mutex);

            AddConnectedThread(new_socket, new_thread);

            pthread_mutex_unlock(&connected_threads_mutex);
        }
    }
}


void closeLocalSockets()
{
    int i;
    for (i = 0; i< connected_friends_length; i++)
    {
        close(connected_friends[i].sock_desc);
    }

    close(local_socket);
}


void terminateFriendThreads()
{
    int i;
    for (i = 0; i< connected_threads_length; i++)
    {
        pthread_cancel(connected_threads[i].pid);
    }

    connected_threads_length = 0;

}


void termination_handler(int sig_num)
{
    char cmd[] = "terminate";
    write(server_socket, cmd, sizeof(cmd));
    exitHandler();
}


void exitHandler()
{
    if(logged_in == 1)
    {
        terminateFriendThreads();
        closeLocalSockets();
    }

    close(server_socket);
    pthread_mutex_destroy(&friends_info_mutex);
    pthread_mutex_destroy(&sent_invites_mutex);
    pthread_mutex_destroy(&received_invites_mutex);
    pthread_mutex_destroy(&connected_friends_mutex);
    pthread_mutex_destroy(&connected_threads_mutex);
}


void displayCommands()
{
    if(logged_in == 0)
    {
        printf("REGISTER -> r [username] [password]\n");
        printf("LOGIN    -> l [username] [password]\n");
        printf("EXIT     -> exit \n");
    }
    else
    {
        printf("Send message to friend  -> message [friend username] [message]\n");
        printf("Send friend invite      -> invite [username] [optional message]\n");
        printf("Accept friend invite    -> accept [username] [optional message]\n");
        printf("Logout of the server    -> logout\n");
    }

    printf("Display commands     -> help\n");
}


int SendMessage(char *username, char*message)
{
    int i;
    int s_desc;
    int flag = 0;
    char buffer[256];

    pthread_mutex_lock(&connected_friends_mutex);
    for (i = 0; i < connected_friends_length; i++)
    {
        if(strcmp(connected_friends[i].username, username) == 0)
        {
            s_desc = connected_friends[i].sock_desc;
            flag = 1;
        }
    }
    //if friend is not connected, make connection first
    if(flag == 0)
    {
        char *ip_address;
        char *port;

        pthread_mutex_lock(&friends_info_mutex);    
        for (i=0 ; i<friends_info_length; i++)
        {
            if(strcmp(friends_info[i].username, username) == 0)
            {
                ip_address = (char *)malloc(sizeof(char) * (strlen(friends_info[i].ip_address)+1));
                strcpy(ip_address,friends_info[i].ip_address);

                sprintf(port, "%d", friends_info[i].port);
            }
        }
        pthread_mutex_unlock(&friends_info_mutex);

        struct addrinfo hints;
        struct addrinfo *info;        
        int new_socket;
        struct sockaddr_in sock_addr;

        memset(&hints, 0, sizeof(hints));
        hints.ai_family = AF_INET;
        hints.ai_socktype = SOCK_STREAM;
        hints.ai_flags = AI_CANONNAME;

        if(getaddrinfo(ip_address, port, &hints, &info) != 0)
        {
            printf("\nFailed to get address info of %s", username);
            return -1;
        }

        //create socket
        if((new_socket = socket(info->ai_family, info->ai_socktype, info->ai_protocol)) < 0)
        {
            printf("\nFailed creating socket for %s", username);
            return -1;
        }


        if (connect(new_socket, info->ai_addr, info->ai_addrlen) < 0) 
        {
            printf("\nFailed to connect to friend %s", username);
            return -1;
        }

        s_desc = new_socket;

        //let friend know of connection

        snprintf(buffer, sizeof(buffer), "user %s", username);
        write(s_desc, buffer, sizeof(buffer));

        //create thread to handle messages from newly created friend

        pthread_t new_friend_thread;

        if(pthread_create(&new_friend_thread, &detached_thread_attr, handleFriend, (void*)&s_desc) != 0 )
        {
            printf("\nCould not create thread for friend %s", username);
            return -1;
        }

        pthread_mutex_lock(&connected_threads_mutex);

        connected_threads[connected_threads_length].socket = s_desc;
        connected_threads[connected_threads_length].pid = new_friend_thread;
        connected_threads_length = connected_threads_length + 1;

        pthread_mutex_unlock(&connected_threads_mutex);

        connected_friends[connected_friends_length].username = (char *)malloc(sizeof(char) * (strlen(username)+1));
        strcpy(connected_friends[connected_friends_length].username, username);
        connected_friends[connected_friends_length].sock_desc = s_desc;     
        connected_friends_length = connected_friends_length + 1;  

    }
    pthread_mutex_unlock(&connected_friends_mutex);
    strncpy(buffer, message, sizeof(buffer));
    write(s_desc, buffer,sizeof(buffer));

    return 0;

}


int AlreadyFriend(char *username)
{
    int i;
    for (i=0 ; i<friends_info_length; i++)
        {
            if(strcmp(friends_info[i].username, username) == 0)
            {
                return -1;
            }
        }
    return 0;
}


int HasSentInvite(char *username)
{
    int i;
    for (i=0 ; i< sent_invites_length; i++)
        {
            if(strcmp(sent_invites[i], username) == 0)
            {
                return -1;
            }
        }
    return 0;
}

               
int ReceivedInviteFrom(char *username)
{
    int i;
    for (i=0 ; i< received_invites_length; i++)
        {
            if(strcmp(received_invites[i], username) == 0)
            {
                return -1;
            }
        }
    return 0;
}


void RemoveInvite(char *username)
{
    int i;
    int pos;
    for (i=0 ; i< received_invites_length; i++)
        {
            if(strcmp(received_invites[i], username) == 0)
            {
                pos = i;
            }
        }

    for( i = pos; i< received_invites_length; i++)
    {
        received_invites[i] = received_invites[i+1];
    }

    received_invites_length = received_invites_length - 1;    

}


void AddFriendInfo(char *username, char * ip_address, char *port)
{
    friends_info[friends_info_length].username = (char *)malloc(sizeof(char) * (strlen(username)+1));
    strcpy(friends_info[friends_info_length].username, username);

    friends_info[friends_info_length].ip_address = (char *)malloc(sizeof(char) * (strlen(ip_address)+1));
    strcpy(friends_info[friends_info_length].ip_address, ip_address);

    friends_info[friends_info_length].port = atoi(port);

    friends_info_length = friends_info_length + 1;
}


void AddReceivedInvite(char *username)
{
    received_invites[received_invites_length] = (char *)malloc(sizeof(char) * (strlen(username)+1));
    strcpy(received_invites[received_invites_length], username);

    received_invites_length = received_invites_length + 1;
}


void  removeSentInvite(char *username)
{
    int i;
    int pos;
    for (i=0 ; i< sent_invites_length; i++)
        {
            if(strcmp(sent_invites[i], username) == 0)
            {
                pos = i;
            }
        }

    for( i = pos; i< received_invites_length; i++)
    {
        sent_invites[i] = sent_invites[i+1];
    }

    sent_invites_length = sent_invites_length - 1;   

}


void RemoveFriendInfo(char *username)
{

    int i;
    int pos;
    for (i=0 ; i<friends_info_length; i++)
        {
            if(strcmp(friends_info[i].username, username) == 0)
            {
                pos = i;
            }
        }

    for(i = pos; i < friends_info_length; i++)
    {
        friends_info[i] = friends_info[i+1];
    }

    friends_info_length = friends_info_length - 1;

}
           

void RemoveConnectedFriend(char *username)
{
    int i;
    int s_desc;
    int flag = 0;
    int pos;

    
    for (i = 0; i < connected_friends_length; i++)
    {
        if(strcmp(connected_friends[i].username, username) == 0)
        {
            s_desc = connected_friends[i].sock_desc;
            flag = 1;
            pos = i;

        }
    }

    if(flag == 1)
    {
        for (i = pos; i < connected_friends_length; i++)
        {
            connected_friends[i] = connected_friends[i+1];
        }
        connected_friends_length = connected_friends_length - 1;

    }
}


void PrintMessageFromFriend(int sock, char *message)
{
    int i;
    for (i = 0; i < connected_friends_length; i++)
    {
        if(connected_friends[i].sock_desc == sock)
        {
            printf("[%s] : %s", connected_friends[i].username, message);
            return;

        }

    }
}


void AddConnectedThread(int sock, pthread_t pid)
{
    connected_threads[connected_threads_length].socket = sock;
    connected_threads[connected_threads_length].pid = pid;

    connected_threads_length = connected_threads_length + 1;
}



















// void *receiveMessage(void *socket_desc)
// {
//     //Get the socket descriptor
//     int sock = *(int*)socket_desc;
//     int read_size;
//     char server_message[2000];

//     while( (read_size = recv(sock , server_message , 2000 , 0)) > 0 )
//     {
        
//         server_message[read_size-1] = '\0';       //end of string marker
//         printf("%s", server_message);   

//         if(strcmp(server_message, "500") == 0)
//         {
//             printf("Invalid username and password. Please enter values again.");
//         } 

//         else if(strcmp(server_message, "200") == 0)   
//         {
//             char *c_ip = "127.0.0.1";
//             int c_port = 5100;
//             int s_desc, c_sock, c;
//             struct sockaddr_in client, other_client;
//             //Create socket
//             s_desc = socket(AF_INET , SOCK_STREAM , 0);
//             if (s_desc == -1)
//             {
//                 printf("Could not create socket");
//             }
//             puts("Socket created");

//             //Prepare the sockaddr_in structure
//             client.sin_family = AF_INET;
//             client.sin_addr.s_addr = INADDR_ANY;
//             client.sin_port = htons( c_port );

//             //Bind
//             if( bind(s_desc,(struct sockaddr *)&client , sizeof(client)) < 0)
//             {
//             //print the error message
//             perror("bind failed. Error");
//             //return 1;
//             }
//             puts("binding done");

//             //Listen
//             listen(s_desc , 3);

//             puts("Waiting for incoming connections...");
//             c = sizeof(struct sockaddr_in);

//             //send connection info to the server

//             write(sock, c_ip, strlen(c_ip));
//             write(sock, (char*)itoa(c_port), strlen(itoa(c_port)));           


//             while( (c_sock = accept(s_desc, (struct sockaddr *)&other_client, (socklen_t*)&c)) )
//             {
//             puts("Connection accepted");

//             //here handel chat messages

//             }

//             if (c_sock < 0)
//             {
//             perror("accept failed");
//             //return 1;
//             }

//             close(s_desc);
//         }
//         else
//         {
//             memset(server_message, 0, 2000);        //clear the message buffer
//         }
        
        
//     }

//     if(read_size == 0)
//     {
//         puts("Server disconnected");
//         fflush(stdout);
//     }

//     else if(read_size == -1)
//     {
//         perror("recv failed");
//     }
         
//     //return 0;
// }
