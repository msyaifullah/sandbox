import React from 'react';

interface ProgressBarProps {
  progress: number;
  isStreaming: boolean;
  receivedFlights?: number;
  totalExpected?: number;
}

const ProgressBar: React.FC<ProgressBarProps> = ({ 
  progress, 
  isStreaming, 
  receivedFlights, 
  totalExpected 
}) => {
  const getProgressText = () => {
    if (isStreaming) {
      if (receivedFlights && totalExpected) {
        return `Receiving flight results... (${receivedFlights} flights)`;
      }
      return 'Receiving flight results...';
    }
    return 'Searching for flights...';
  };

  return (
    <div className="progress-container">
      <div className="progress-info">
        <span className="progress-text">
          {getProgressText()}
        </span>
        <span className="progress-percentage">{Math.round(progress)}%</span>
      </div>
      <div className="progress-bar">
        <div 
          className="progress-fill"
          style={{ width: `${progress}%` }}
        />
      </div>
      {isStreaming && (
        <div className="streaming-indicator">
          <div className="pulse-dot"></div>
          <span>Live streaming results</span>
        </div>
      )}
    </div>
  );
};

export default ProgressBar; 