#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>

#define MAX_SIZE 50
void *receiveMessage(void *);
int main()
{
    int sock_desc;
    int server_sock;
    struct sockaddr_in serv_addr;
    char sbuff[MAX_SIZE],rbuff[MAX_SIZE];
    pthread_t thread_id;


    if((sock_desc = socket(AF_INET, SOCK_STREAM, 0)) < 0)
        printf("Failed creating socket\n");

    bzero((char *) &serv_addr, sizeof (serv_addr));

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = inet_addr("127.0.0.1");
    serv_addr.sin_port = htons(5558);

    if (connect(sock_desc, (struct sockaddr *) &serv_addr, sizeof (serv_addr)) < 0) {
        printf("Failed to connect to server\n");
        return -1;
    }

    if( pthread_create( &thread_id , NULL , receiveMessage , (void*) &sock_desc) > 0)
        {
            perror("could not create thread");
            return 1;
        }

    while(fgets(sbuff, MAX_SIZE , stdin)!=NULL)
    {
      //send(sock_desc,sbuff,strlen(sbuff),0);
      printf("%s", sbuff);
      write(sock_desc, sbuff, strlen(sbuff));   
      memset(sbuff, 0, strlen(sbuff));

    }

    close(sock_desc);
    return 0;

}

void *receiveMessage(void *socket_desc)
{
    //Get the socket descriptor
    int sock = *(int*)socket_desc;
    int read_size;
    char server_message[2000];

    while( (read_size = recv(sock , server_message , 2000 , 0)) > 0 )
    {
        
        server_message[read_size-1] = '\0';       //end of string marker
        printf("%s", server_message);   

        if(strcmp(server_message, "500") == 0)
        {
            printf("Invalid username and password. Please enter values again.");
        } 

        else if(strcmp(server_message, "200") == 0)   
        {
            char *c_ip = "127.0.0.1";
            int c_port = 5100;
            int s_desc, c_sock, c;
            struct sockaddr_in client, other_client;
            //Create socket
            s_desc = socket(AF_INET , SOCK_STREAM , 0);
            if (s_desc == -1)
            {
                printf("Could not create socket");
            }
            puts("Socket created");

            //Prepare the sockaddr_in structure
            client.sin_family = AF_INET;
            client.sin_addr.s_addr = INADDR_ANY;
            client.sin_port = htons( c_port );

            //Bind
            if( bind(s_desc,(struct sockaddr *)&client , sizeof(client)) < 0)
            {
            //print the error message
            perror("bind failed. Error");
            //return 1;
            }
            puts("binding done");

            //Listen
            listen(s_desc , 3);

            puts("Waiting for incoming connections...");
            c = sizeof(struct sockaddr_in);

            //send connection info to the server

            write(sock, c_ip, strlen(c_ip));
            write(sock, (char*)itoa(c_port), strlen(itoa(c_port)));           


            while( (c_sock = accept(s_desc, (struct sockaddr *)&other_client, (socklen_t*)&c)) )
            {
            puts("Connection accepted");

            //here handel chat messages

            }

            if (c_sock < 0)
            {
            perror("accept failed");
            //return 1;
            }

            close(s_desc);
        }
        else
        {
            memset(server_message, 0, 2000);        //clear the message buffer
        }
        
        
    }

    if(read_size == 0)
    {
        puts("Server disconnected");
        fflush(stdout);
    }

    else if(read_size == -1)
    {
        perror("recv failed");
    }
         
    //return 0;
}
