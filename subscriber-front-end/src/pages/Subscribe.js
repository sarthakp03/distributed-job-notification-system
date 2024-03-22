import { useState } from 'react';
import { Card, Form, Button } from 'react-bootstrap';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';

const Subscribe = () => {
    //const completeSubURL = "http://ec2-54-82-71-245.compute-1.amazonaws.com:8082";
    //const subURL = "ec2-54-82-71-245.compute-1.amazonaws.com";
    const completeSubURL = "http://localhost:8082";
    const subURL = "localhost";
    const location = useLocation();
    const queryParams = new URLSearchParams(location.search);
    const listOfTopics = queryParams.get('list');
    const topicsArray = listOfTopics ? listOfTopics.split(',') : [];
    const [checkedItems, setCheckedItems] = useState({});
    const history = useNavigate();

    const handleCheckboxChange = (event) => {
        const { name, checked } = event.target;
        setCheckedItems((prevCheckedItems) => ({
          ...prevCheckedItems,
          [name]: checked,
        }));
      };

    const handleSubmit = (e) => {
        e.preventDefault();
        console.log(checkedItems);

        const url = completeSubURL + '/subscribe';
        let subscriberID = Math.random();
        let subscriberPort = 8082;
        const topics = [];

        for (const key in checkedItems) {
            topics.push(key);
        }

        const subscriptionDetails = { 
            subscriberId: 1,
            port: subscriberPort,
            companyNames: topics,
            awsUrl: subURL
        };

        axios.post(url, subscriptionDetails).then((response) => {
            console.log(response.data);
            if (response.status === 200) {
                console.log("Company name send to backend successfully");
            } else if (response.status === 400 || response.status === 404) {
                console.log("Publisher not found");
            } else if (response.status === 500) {
                console.log("Publisher is not reachable");
            }
          }).catch(err => {
            console.log(err);
          });

        const searchParams = new URLSearchParams();
        searchParams.append('id', subscriberID);
        searchParams.append('port', subscriberPort);
        searchParams.append('topics', topics);

        history({
            pathname: '/jobs',
            search: searchParams.toString()
        });
    }

    return (
        <>
            <div className="centered">
                <Card className="text-center">
                    <Card.Header>Subscription</Card.Header>
                    <Card.Body className="custom-card-body">
                        <Form onSubmit={handleSubmit}>
                            {topicsArray.map((topic) => (
                                <Form.Check
                                    key={topic}
                                    type="checkbox"
                                    id={topic}
                                    label={topic}
                                    name={topic}
                                    checked={checkedItems[topic] || false}
                                    onChange={handleCheckboxChange}
                                />
                            ))}
                            <hr />
                            <Button variant="success" type="submit">Done</Button>
                        </Form>
                    </Card.Body>
                    <Card.Footer className="text-muted">(c) Ruchi | Ann | Prachi</Card.Footer>
                </Card>
            </div>
        </>
    );
}

export default Subscribe;
