import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { Button, Card } from 'react-bootstrap';

const Home = () => {
    //const brokerURL = "http://ec2-54-82-71-245.compute-1.amazonaws.com:8082";
    const brokerURL = "http://localhost:8082";

    const history = useNavigate();

    const handleClick = (e) => {
        e.preventDefault();
        const url = brokerURL + '/get/topics';
        const topics = [];
        
        axios.get(url).then((response) => {
            for (const key in response.data) {
                const value = response.data[key];
                topics.push(value);
            }

            const searchParams = new URLSearchParams();
            searchParams.append('list', topics);

            history({
                pathname: '/subscribe',
                search: searchParams.toString()
            });

        }).catch(err => {
            console.log(err);
        });
    }

    return (
        <>
            <div className="centered">
                <Card className="text-center">
                    <Card.Header>Job Notification Application</Card.Header>
                    <Card.Body>
                        <Card.Title>One Stop to Get Notified About New Jobs of Your Favourite Companies</Card.Title>
                        <Card.Text>
                            Streamline Your Job Notification Subscription: Sign Up On Our App Today
                        </Card.Text>
                        <Button variant="dark" onClick={handleClick}>Get Started</Button>
                    </Card.Body>
                    <Card.Footer className="text-muted">(c) Ruchi | Ann | Prachi</Card.Footer>
                </Card>
            </div>
        </>
    );
}

export default Home;
