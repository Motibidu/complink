import { useState, useEffect } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'

function App() {
  const [data, setData] = useState("아직 불러오지 못함")

   
    useEffect(()=>{
      const Init = async () => {
      try {
        const response = await fetch('/api/home/init');
        const parsedResponse = await response.text(); 
        setData(parsedResponse);
        console.log('백엔드에서 받은 데이터: ', parsedResponse.message);
      } catch (err) {
        console.log('데이터를 불러오는 중 오류 발생:', err);
      } finally {
        console.log('데이터 불러오기 종료');
      }
    };
    Init();
    }, []);

  return (
    <>
      <div>
        <a href="https://vite.dev" target="_blank">
          <img src={viteLogo} className="logo" alt="Vite logo" />
        </a>
        <a href="https://react.dev" target="_blank">
          <img src={reactLogo} className="logo react" alt="React logo" />
        </a>
      </div>
      <h1>Vite + React</h1>
      <div className="card">
        <p>{data}</p>
        <p>
          Edit <code>src/App.jsx</code> and save to test HMR
        </p>
      </div>
      <p className="read-the-docs">
        Click on the Vite and React logos to learn more
      </p>
    </>
  )
}

export default App
