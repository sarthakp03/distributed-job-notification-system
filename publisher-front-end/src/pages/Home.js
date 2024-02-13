import React from 'react';
import { useNavigate } from "react-router-dom";
import { Button, Card } from 'react-bootstrap';

const Home = () => {
    const navigate = useNavigate();
    return (
        <>
            <div className="centered">
                <Card className="text-center">
                    <Card.Header>Job Notification Application</Card.Header>
                    <Card.Body>
                        <Card.Title>One Stop to Add New Jobs</Card.Title>
                        <Card.Text>
                            Streamline Your Job Posting Process: Sign Up On Our App Today
                        </Card.Text>
                        <Button variant="dark" onClick={() => navigate("/register")}>Register Your Company</Button>
                    </Card.Body>
                    <Card.Footer className="text-muted">(c) Ruchi | Ann | Prachi</Card.Footer>
                </Card>
            </div>
        </>
    );
}

export default Home;
