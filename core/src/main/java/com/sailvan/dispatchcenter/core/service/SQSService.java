package com.sailvan.dispatchcenter.core.service;

import com.amazonaws.regions.Regions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

@Component
public class SQSService {

    private static final Logger logger = LoggerFactory.getLogger(SQSService.class);
    /**
     * 创建队列
     * @param sqsClient
     * @param queueName
     * @return 队列URL
     */
    public static String createQueue(SqsClient sqsClient,String queueName ) {

        try {
            System.out.println("\nCreate Queue");

            CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
                    .queueName(queueName)
                    .build();

            sqsClient.createQueue(createQueueRequest);
            System.out.println("\nGet queue url");

            GetQueueUrlResponse getQueueUrlResponse =
                    sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build());
            String queueUrl = getQueueUrlResponse.queueUrl();
            return queueUrl;

        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return "";
    }

    /**
     * 查询所有的队列
     * @param sqsClient
     */
    public static void listQueues(SqsClient sqsClient) {

        System.out.println("\nList Queues");

        try {
            ListQueuesRequest listQueuesRequest = ListQueuesRequest.builder().build();
            ListQueuesResponse listQueuesResponse = sqsClient.listQueues(listQueuesRequest);

            for (String url : listQueuesResponse.queueUrls()) {
                System.out.println(url);
            }

        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    /**
     * 通过队列名的前缀，带条件查询所有的队列
     * @param sqsClient
     * @param namePrefix
     */
    public static void listQueuesFilter(SqsClient sqsClient, String namePrefix) {
        try {
            ListQueuesRequest filterListRequest = ListQueuesRequest.builder()
                    .queueNamePrefix(namePrefix).build();

            ListQueuesResponse listQueuesFilteredResponse = sqsClient.listQueues(filterListRequest);

            for (String url : listQueuesFilteredResponse.queueUrls()) {
                System.out.println(url);
            }
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }


    /**
     * 批量发送消息
     * @param sqsClient
     * @param queueUrl
     */
    public static void sendBatchMessages(SqsClient sqsClient, String queueUrl) {
        System.out.println("\nSend multiple messages");

        try {
            SendMessageBatchRequest sendMessageBatchRequest = SendMessageBatchRequest.builder()
                    .queueUrl(queueUrl)
                    .entries(SendMessageBatchRequestEntry.builder().id("id1").messageBody("Hello from msg 1").build(),
                            SendMessageBatchRequestEntry.builder().id("id2").messageBody("msg 2").delaySeconds(10).build())
                    .build();
            sqsClient.sendMessageBatch(sendMessageBatchRequest);
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    /**
     * 消费消息
     * @param sqsClient
     * @param queueUrl
     * @return
     */
    public static  List<Message> receiveMessages(SqsClient sqsClient, String queueUrl) {
        try {
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(5)
                    .build();
            List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();
            return messages;
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return null;
    }

    /**
     * 改变消息的可见性超时，即这段时间内一个消费者进行消费时其他消费者对消息不可见。
     * @param sqsClient
     * @param queueUrl
     * @param messages
     */
    public static void changeMessages(SqsClient sqsClient, String queueUrl, List<Message> messages) {
        try {

            for (Message message : messages) {
                ChangeMessageVisibilityRequest req = ChangeMessageVisibilityRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .visibilityTimeout(100)
                        .build();
                sqsClient.changeMessageVisibility(req);
            }
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    /**
     * 删除消息
     * @param sqsClient
     * @param queueUrl
     * @param messages
     */
    public static void deleteMessages(SqsClient sqsClient, String queueUrl,  List<Message> messages) {
        try {
            for (Message message : messages) {
                DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build();
                sqsClient.deleteMessage(deleteMessageRequest);
            }

        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }


    public static void main(String[] args) {
//        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
//                "AKIAXUDALMYBBAIHPO53",
//                "uqbsP8PQox9CCb1wa6bhgDPgas8pWWwqo+n20pXq");
//        Region region = Region.US_EAST_2;
//        Region regions = Region.of("us-east-2");
//        SqsClient sqsClient = SqsClient.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds))
//                .region(region)
//                .build();
//
//        List<Message> messages = receiveMessages(sqsClient, "https://sqs.us-east-2.amazonaws.com/524188083714/test");
//
//        for (Message message:messages) {
//            System.out.println(message.body());
//        }
    }
}
