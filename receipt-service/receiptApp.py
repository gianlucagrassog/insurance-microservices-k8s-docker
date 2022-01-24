from flask import Flask
from kafka import KafkaConsumer,KafkaProducer
from kafka.errors import KafkaError
from fix_nginx import ReverseProxied
import json
import time
import os
import smtplib
import threading
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

app = Flask(__name__)
app.wsgi_app = ReverseProxied(app.wsgi_app)

BOOTSTRAP_SERVERS = [str(os.environ.get('KAFKA_ADDRESS'))]

topic_in = str(os.environ.get('KAFKA_TOPIC_IN'))
topic_out = str(os.environ.get('KAFKA_TOPIC_OUT'))

def send_email(email_data):

    policy_cost=float(email_data[3])-float(email_data[4])
    sender_address = str(os.environ.get('INSURANCE_EMAIL'))
    sender_pass = str(os.environ.get('EMAIL_PW'))
    receiver_address = 'rino.dipaola@gmail.com' #change with your email
    message = MIMEMultipart()
    message['from'] = sender_address
    message['ro'] = receiver_address
    message['subject'] = 'Policy purchase confirmation - Purchase number: '+email_data[0]

    message.attach(MIMEText("""Hi %s,
Your policy %s will cost %s$.
Purchase Details:
    - Policy base price %s$;
    - Optionals price %s$;
Total: %s$.
    
If there is any problem, feel free to contact us.
Thanks for choosing us , 
Insurance company x""" % (email_data[1],email_data[2],email_data[3],policy_cost,email_data[4],email_data[3])))

    session = smtplib.SMTP('smtp.gmail.com', 587)
    session.starttls() #enable security
    session.login(sender_address, sender_pass)
    text = message.as_string()
    result = True
    try:
        session.sendmail(sender_address, receiver_address, text)
    except:
        print("An exception occurred")
        result = False
    session.quit()
    return result

@app.route('/ping')
def hello_world():
    return 'pong'

def register_kafka_listener(topic, listener):
    # Poll kafka
    def poll():
        print("Thread partito")
        # Initialize consumer Instance
        consumer = KafkaConsumer(topic, bootstrap_servers=BOOTSTRAP_SERVERS,
                                 api_version=(0, 10, 1))
        while True:
            consumer.poll(timeout_ms=5000)
            for msg in consumer:
                print("Entered the loop\nKey: ",msg.key," Value:", msg.value)
                kafka_listener(msg)
            time.sleep(10);
    print("Thread ")
    t1 = threading.Thread(target=poll)
    t1.start()

def kafka_listener(data):
    print("Valore:\n", data.value.decode("utf-8"))
    email_data = (data.value.decode("utf-8")).split("|")
    res = send_email(email_data)
    producer = KafkaProducer(bootstrap_servers=BOOTSTRAP_SERVERS,
                             api_version=(0, 10, 1),
                             key_serializer=str.encode)

    if(res == True):
        msg=('mailsent'+'|'+email_data[0]).encode("utf-8")
    else:
        msg=('mailnotsent'+'|'+email_data[0]).encode("utf-8")
    producer.send('receipt-purchase-topic', key='ReceiptResponse', value=msg)

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', threaded=True)

print("Valoreaaa")
print(topic_in)
print(topic_out)
register_kafka_listener('purchase-receipt-topic', kafka_listener)

